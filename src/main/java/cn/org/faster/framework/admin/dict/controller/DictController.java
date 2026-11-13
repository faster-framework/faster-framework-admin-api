package cn.org.faster.framework.admin.dict.controller;

import cn.org.faster.framework.admin.common.model.PageResult;
import cn.org.faster.framework.admin.dict.entity.SysDict;
import cn.org.faster.framework.admin.dict.model.DictQuery;
import cn.org.faster.framework.admin.dict.model.SysDictReq;
import cn.org.faster.framework.admin.dict.service.DictService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典接口。
 *
 * @author faster-builder
 */
@RestController
@RequestMapping("/admin/sys/dict")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    /**
     * 字典全量列表（登录即可访问，供下拉框等通用场景使用）。
     */
    @GetMapping("/all")
    public List<SysDict> listAll() {
        return dictService.listAll();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('dict:list')")
    public PageResult<SysDict> list(@ModelAttribute DictQuery query) {
        return dictService.list(query);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('dict:info')")
    public SysDict info(@PathVariable long id) {
        return dictService.info(id);
    }

    @GetMapping("/query")
    @PreAuthorize("hasAuthority('dict:info')")
    public SysDict query(@ModelAttribute DictQuery query) {
        return dictService.query(query);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('dict:add')")
    public void add(@Valid @RequestBody SysDictReq request) {
        dictService.add(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('dict:modify')")
    public void update(@PathVariable long id, @Valid @RequestBody SysDictReq request) {
        dictService.update(id, request);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('dict:delete')")
    public void delete(@RequestBody List<Long> ids) {
        dictService.delete(ids);
    }
}
