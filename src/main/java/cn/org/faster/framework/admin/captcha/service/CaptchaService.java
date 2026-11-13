package cn.org.faster.framework.admin.captcha.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图形验证码服务。
 *
 * <p>验证码答案保存在进程内（一次性消费、按 TTL 过期），图片为 Base64 PNG。
 * 集群部署时可替换为 Redis 存储。</p>
 *
 * @author zhangbowen
 */
@Service
public class CaptchaService {

    private static final char[] ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    private static final int CLEANUP_THRESHOLD = 1024;

    private record Challenge(String answer, Instant expiresAt) {
    }

    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();

    private final CaptchaProperties properties;

    private final Clock clock;

    private final SecureRandom random = new SecureRandom();

    public CaptchaService(CaptchaProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * 生成验证码。
     *
     * @return token 与 Base64 图片
     */
    public CaptchaChallenge create() {
        cleanupIfNecessary();
        String answer = randomAnswer();
        String token = UUID.randomUUID().toString();
        challenges.put(token, new Challenge(answer, clock.instant().plus(properties.ttl())));
        return new CaptchaChallenge(token, renderBase64(answer));
    }

    /**
     * 校验验证码；无论成功与否 token 都会失效，避免被暴力重试。
     *
     * @param answer 用户输入
     * @param token  验证码 token
     * @return 是否通过
     */
    public boolean validate(String answer, String token) {
        if (answer == null || token == null) {
            return false;
        }
        Challenge challenge = challenges.remove(token);
        if (challenge == null || challenge.expiresAt().isBefore(clock.instant())) {
            return false;
        }
        return challenge.answer().equalsIgnoreCase(answer.trim());
    }

    private String randomAnswer() {
        StringBuilder builder = new StringBuilder(properties.length());
        for (int i = 0; i < properties.length(); i++) {
            builder.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return builder.toString();
    }

    private String renderBase64(String answer) {
        int width = properties.width();
        int height = properties.height();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(245, 247, 250));
            graphics.fillRect(0, 0, width, height);
            // 干扰线
            for (int i = 0; i < 6; i++) {
                graphics.setColor(randomColor(150, 220));
                graphics.drawLine(random.nextInt(width), random.nextInt(height),
                        random.nextInt(width), random.nextInt(height));
            }
            // 字符
            int step = width / (answer.length() + 1);
            for (int i = 0; i < answer.length(); i++) {
                graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(20, height - 12)));
                graphics.setColor(randomColor(20, 120));
                int x = step * (i + 1) - step / 3;
                int y = height - 10 - random.nextInt(6);
                graphics.drawString(String.valueOf(answer.charAt(i)), x, y);
            }
            // 噪点
            for (int i = 0; i < 40; i++) {
                image.setRGB(random.nextInt(width), random.nextInt(height), randomColor(0, 255).getRGB());
            }
        } finally {
            graphics.dispose();
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException ex) {
            throw new UncheckedIOException("验证码图片生成失败", ex);
        }
    }

    private Color randomColor(int min, int max) {
        int bound = max - min;
        return new Color(min + random.nextInt(bound), min + random.nextInt(bound), min + random.nextInt(bound));
    }

    private void cleanupIfNecessary() {
        if (challenges.size() < CLEANUP_THRESHOLD) {
            return;
        }
        Instant now = clock.instant();
        challenges.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }
}
