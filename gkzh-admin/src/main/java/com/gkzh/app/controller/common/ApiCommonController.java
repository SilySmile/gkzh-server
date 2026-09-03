package com.gkzh.app.controller.common;

import com.gkzh.activity.domain.GkzhActivity;
import com.gkzh.activity.service.IGkzhActivityService;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.entity.SysDictData;
import com.gkzh.common.utils.StringUtils;
import com.gkzh.common.utils.WeixinJssdkUtil;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.service.IGkzhSchoolDepartmentService;
import com.gkzh.school.service.IGkzhSchoolService;
import com.gkzh.system.service.ISysDictTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api("公共接口")
@RestController
@RequestMapping("/api/common")
public class ApiCommonController extends BaseController {

    @Autowired
    private IGkzhSchoolService gkzhSchoolService;
    @Autowired
    private IGkzhSchoolDepartmentService departmentService;

    @Autowired
    private IGkzhActivityService gkzhActivityService;

    @Autowired
    private WeixinJssdkUtil weixinJssdkUtil;
    @Autowired
    private ISysDictTypeService dictTypeService;
    @ApiOperation("学校列表")
    @GetMapping("/school/{activityId}")
    public AjaxResult school(@PathVariable Long activityId)
    {
        GkzhActivity gkzhActivity = gkzhActivityService.selectGkzhActivityByActivityId(activityId);

        GkzhSchool gkzhSchool = new GkzhSchool();
        if (gkzhActivity != null && gkzhActivity.getOrganizer() != null){
            Long schoolId = Long.valueOf(gkzhActivity.getOrganizer());
            gkzhSchool.setSchoolId(schoolId);
        }else{
            return AjaxResult.error("活动不存在");
        }
        List<GkzhSchool> list = gkzhSchoolService.selectGkzhSchoolList(gkzhSchool);
        return AjaxResult.success(list);
    }

    /**
     * 获取部门树结构
     */
    @ApiOperation("部门列表")
    @GetMapping("/tree/{schoolId}")
    public AjaxResult getDepartmentTree(@PathVariable Long schoolId) {
        List<GkzhSchoolDepartment> departments = departmentService.selectDepartmentTreeBySchoolId(schoolId);
        return AjaxResult.success(departmentService.buildDepartmentTree(departments));
    }

    

    /**
     * 获取微信JS-SDK配置
     * 
     * @param url 当前页面URL
     * @return 微信JS-SDK配置参数
     */
    @GetMapping("/jssdk/config")
    public AjaxResult getJssdkConfig(@RequestParam String url) {
        try {
            Map<String, Object> config = weixinJssdkUtil.getJssdkConfig(url);
            return AjaxResult.success(config);
        } catch (Exception e) {
            return AjaxResult.error("获取微信配置失败：" + e.getMessage());
        }
    }

    @ApiOperation("字典数据")
    @GetMapping("/dict/data/type/{dictType}")
    public AjaxResult dictType(@PathVariable String dictType)
    {
        List<SysDictData> data = dictTypeService.selectDictDataByType(dictType);
        if (StringUtils.isNull(data))
        {
            data = new ArrayList<SysDictData>();
        }
        return success(data);
    }


    @ApiOperation("全部学校列表")
    @GetMapping("/schools")
    public AjaxResult allSchools() {
        List<GkzhSchool> list = gkzhSchoolService.selectGkzhSchoolList(new GkzhSchool());
        return AjaxResult.success(list);
    }


}
