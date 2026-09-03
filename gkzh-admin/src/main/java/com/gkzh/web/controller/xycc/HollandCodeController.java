package com.gkzh.web.controller.xycc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.xycc.domain.HollandCode;
import com.gkzh.xycc.service.IHollandCodeService;

@RestController
@RequestMapping("/xycc/holland")
public class HollandCodeController extends BaseController {

    @Autowired
    private IHollandCodeService hollandCodeService;

    @GetMapping("/list")
    public AjaxResult list() {
        return AjaxResult.success(hollandCodeService.listCodes());
    }

    @GetMapping("/{code}")
    public AjaxResult getInfo(@PathVariable String code) {
        return AjaxResult.success(hollandCodeService.getCode(code));
    }

    @PostMapping
    public AjaxResult add(@RequestBody HollandCode code) {
        return toAjax(hollandCodeService.saveCode(code));
    }

    @DeleteMapping("/{code}")
    public AjaxResult remove(@PathVariable String code) {
        return toAjax(hollandCodeService.deleteCode(code));
    }
}
