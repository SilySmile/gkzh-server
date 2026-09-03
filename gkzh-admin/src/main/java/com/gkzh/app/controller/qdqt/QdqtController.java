package com.gkzh.app.controller.qdqt;

import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.qdqt.dto.*;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.qdqt.service.IGkzhStudentCheckinService;
import com.gkzh.qdqt.service.StuTokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.service.IGkzhStudentService;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 签到签退Controller
 * 
 * @author gkzh
 * @date 2025-06-22
 */
@Api("签到签退")
@RestController
@RequestMapping("/api/student")
public class QdqtController extends FrontBaseController
{
    @Autowired
    private IGkzhStudentCheckinService studentAuthService;
    @Autowired
    private StuTokenService stuTokenService;
    @Autowired
    private IGkzhStudentCheckinService checkinService;
    @Autowired
    private IGkzhStudentService studentService;

    @ApiOperation("获取个人资料")
    @GetMapping("/profile")
    public AjaxResult profile() {
        StudentCheckin currentStudent = getCurrentStudent();
        if (currentStudent == null || currentStudent.getStuId() == null) {
            return AjaxResult.error("未登录");
        }
        GkzhStudent student = studentService.selectGkzhStudentByStudentId(currentStudent.getStuId());
        if (student == null) {
            return AjaxResult.error("学生不存在");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studentName", student.getStudentName());
        data.put("studentNo", student.getStudentNo());
        data.put("schoolId", student.getSchoolId());
        data.put("schoolName", student.getSchoolName());
        data.put("departmentName", student.getDepartmentName());
        data.put("gender", student.getGender());
        return AjaxResult.success(data);
    }
    @ApiOperation("签到")
    @PostMapping("/checkin")
    public AjaxResult login(@RequestBody StudentLoginRequest request) {
        StudentTokenResponse checkin = studentAuthService.checkin(request);
        return AjaxResult.success(checkin);
    }

    @ApiOperation("签退")
    @PostMapping("/signout")
    public AjaxResult signout(@RequestBody SignoutRequest request) {
        StudentCheckin currentStudent = getCurrentStudent();
        boolean result = checkinService.signout(currentStudent,request.getActivityId());
        return result ? AjaxResult.success("签退成功") : AjaxResult.error("签退失败");
    }

    @ApiOperation("退出")
    @GetMapping("/logout")
    public AjaxResult logout() {
        //删除用户缓存记录
        StudentCheckin currentStudent = getCurrentStudent();
        stuTokenService.delLoginUser(currentStudent.getToken());
        return AjaxResult.success("退出成功");
    }

    @ApiOperation("注册")
    @PostMapping("/register")
    public AjaxResult register(@RequestBody StudentRegisterRequest request) {
        StudentTokenResponse result = checkinService.register(request);
        return AjaxResult.success(result);
    }

    @ApiOperation("登录")
    @PostMapping("/login")
    public AjaxResult login(@RequestBody StudentLoginSimpleRequest request) {
        StudentTokenResponse result = checkinService.login(request);
        return AjaxResult.success(result);
    }
}
