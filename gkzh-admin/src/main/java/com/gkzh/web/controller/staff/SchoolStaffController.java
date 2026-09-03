package com.gkzh.web.controller.staff;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.gkzh.activity.domain.staff.GkzhSchoolStaff;
import com.gkzh.activity.mapper.staff.GkzhSchoolStaffMapper;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.SecurityUtils;
import com.gkzh.common.utils.StringUtils;

/** 后台配置学校工作人员账号，工作人员只能在小程序端操作所属学校数据。 */
@RestController
@RequestMapping("/staff/account")
public class SchoolStaffController extends BaseController {
    @Autowired private GkzhSchoolStaffMapper staffMapper;

    @PreAuthorize("@ss.hasPermi('school:school:list')")
    @GetMapping("/list")
    public TableDataInfo list() { startPage(); List<GkzhSchoolStaff> list = staffMapper.selectStaffList(); return getDataTable(list); }

    @PreAuthorize("@ss.hasPermi('school:school:add')")
    @PostMapping
    public AjaxResult add(@RequestBody StaffAccountRequest body) {
        if (body.getSchoolId() == null || StringUtils.isEmpty(body.getUserName()) || StringUtils.isEmpty(body.getPassword()) || StringUtils.isEmpty(body.getStaffName())) return error("学校、账号、密码和姓名不能为空");
        if (staffMapper.selectEnabledByUserName(body.getUserName()) != null) return error("工作人员账号已存在");
        GkzhSchoolStaff staff = new GkzhSchoolStaff(); staff.setUserName(body.getUserName()); staff.setPassword(SecurityUtils.encryptPassword(body.getPassword())); staff.setSchoolId(body.getSchoolId()); staff.setStaffName(body.getStaffName()); staff.setStatus("0"); staff.setCreateBy(getUsername()); staff.setCreateTime(new Date()); staffMapper.insert(staff);
        return success(staff);
    }

    @PreAuthorize("@ss.hasPermi('school:school:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody StaffAccountRequest body) {
        if (body.getStaffId() == null || body.getSchoolId() == null || StringUtils.isEmpty(body.getStaffName())) return error("工作人员、学校和姓名不能为空");
        GkzhSchoolStaff staff = staffMapper.selectById(body.getStaffId()); if (staff == null) return error("工作人员不存在");
        staff.setSchoolId(body.getSchoolId()); staff.setStaffName(body.getStaffName()); if (body.getStatus() != null) staff.setStatus(body.getStatus()); if (body.getCanRedeem() != null) staff.setCanRedeem(body.getCanRedeem()); staff.setUpdateBy(getUsername()); staff.setUpdateTime(new Date()); staffMapper.updateById(staff);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('school:school:edit')")
    @PutMapping("/{staffId}/reset-password")
    public AjaxResult resetPassword(@PathVariable Long staffId, @RequestBody StaffAccountRequest body) {
        if (StringUtils.isEmpty(body.getPassword())) return error("新密码不能为空");
        GkzhSchoolStaff staff = staffMapper.selectById(staffId); if (staff == null) throw new ServiceException("工作人员不存在");
        return toAjax(staffMapper.updatePassword(staffId, SecurityUtils.encryptPassword(body.getPassword())));
    }

    public static class StaffAccountRequest {
        private Long staffId; private Long schoolId; private String userName; private String password; private String staffName; private String status; private Integer canRedeem;
        public Long getStaffId() { return staffId; } public void setStaffId(Long v) { staffId=v; }
        public Long getSchoolId() { return schoolId; } public void setSchoolId(Long v) { schoolId=v; }
        public String getUserName() { return userName; } public void setUserName(String v) { userName=v; }
        public String getPassword() { return password; } public void setPassword(String v) { password=v; }
        public String getStaffName() { return staffName; } public void setStaffName(String v) { staffName=v; }
        public String getStatus() { return status; } public void setStatus(String v) { status=v; }
        public Integer getCanRedeem() { return canRedeem; } public void setCanRedeem(Integer v) { canRedeem=v; }
    }
}
