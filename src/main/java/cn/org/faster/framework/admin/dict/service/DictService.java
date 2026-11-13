package cn.org.faster.framework.admin.dict.service;

import cn.org.faster.framework.admin.common.error.BasicErrorCode;
import cn.org.faster.framework.admin.common.error.BusinessException;
import cn.org.faster.framework.admin.common.model.PageResult;
import cn.org.faster.framework.admin.common.util.AuditSupport;
import cn.org.faster.framework.admin.dict.entity.SysDict;
import cn.org.faster.framework.admin.dict.model.DictQuery;
import cn.org.faster.framework.admin.dict.model.SysDictReq;
import cn.org.faster.framework.admin.dict.repository.DictRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典服务。
 *
 * @author faster-builder
 */
@Service
@Transactional
public class DictService {

    private final DictRepository dictRepository;

    private final AuditSupport auditSupport;

    private final Clock clock;

    public DictService(DictRepository dictRepository, AuditSupport auditSupport, Clock clock) {
        this.dictRepository = dictRepository;
        this.auditSupport = auditSupport;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResult<SysDict> list(DictQuery query) {
        List<SysDict> records = dictRepository.page(query);
        long total = dictRepository.count(query);
        return PageResult.of(records, total, query);
    }

    @Transactional(readOnly = true)
    public List<SysDict> listAll() {
        return dictRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SysDict info(long id) {
        return dictRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(BasicErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public SysDict query(DictQuery query) {
        return dictRepository.findFirst(query).orElse(null);
    }

    public void add(SysDictReq request) {
        SysDict dict = new SysDict();
        dict.setName(request.name());
        dict.setType(request.type());
        dict.setDictValue(request.dictValue());
        dict.setRemark(request.remark());
        dict.setShowStatus(request.showStatus() == null ? 1 : request.showStatus());
        auditSupport.markCreated(dict);
        dictRepository.insert(dict);
    }

    public void update(long id, SysDictReq request) {
        info(id);
        SysDict dict = new SysDict();
        dict.setId(id);
        dict.setName(request.name());
        dict.setType(request.type());
        dict.setDictValue(request.dictValue());
        dict.setRemark(request.remark());
        dict.setShowStatus(request.showStatus() == null ? 1 : request.showStatus());
        auditSupport.markUpdated(dict);
        dictRepository.update(dict);
    }

    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        dictRepository.softDelete(ids, auditSupport.currentUserId(), LocalDateTime.now(clock));
    }
}
