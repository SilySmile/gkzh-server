package com.gkzh.app.controller.staff;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.gkzh.activity.domain.staff.GkzhSchoolStaff;
import com.gkzh.activity.dto.staff.StaffPrizeView;
import com.gkzh.activity.mapper.staff.GkzhSchoolStaffMapper;
import com.gkzh.app.core.staff.StaffSession;
import com.gkzh.app.core.staff.StaffTokenService;
import com.gkzh.activity.service.IStaffService;
import com.gkzh.xycc.service.IHollandCodeService;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.SecurityUtils;

@RestController
@RequestMapping("/api/staff")
public class StaffAppController {
    @Autowired
    private StaffTokenService tokenService;
    @Autowired
    private GkzhSchoolStaffMapper staffMapper;
    @Autowired
    private IStaffService staffService;
    @Autowired
    private IHollandCodeService hollandCodeService;

    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        GkzhSchoolStaff staff = staffMapper.selectEnabledByUserName(username);
        if (staff == null || !SecurityUtils.matchesPassword(password, staff.getPassword())) {
            return AjaxResult.error("账号或密码错误");
        }
        if (staff.getStatus().equals("1")) {
            return AjaxResult.error("账号已禁用");
        }
        StaffSession session = new StaffSession();
        session.setStaffId(staff.getStaffId());
        session.setSchoolId(staff.getSchoolId());
        session.setStaffName(staff.getStaffName());
        session.setSchoolName(staff.getSchoolName());
        session.setUserName(staff.getUserName());
        session.setCanRedeem(staff.getCanRedeem());
        String token = tokenService.createToken(session);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("staff", staff);
        data.put("userName", staff.getStaffName());
        return AjaxResult.success(data);
    }

    @GetMapping("/profile")
    public AjaxResult profile(HttpServletRequest request) {
        return AjaxResult.success(staff(request));
    }

    @GetMapping("/prizes")
    public AjaxResult prizes(HttpServletRequest request) {
        GkzhSchoolStaff staff = staff(request);
        ensureCanRedeem(staff);
        return AjaxResult.success(staffService.listPrizeRecords(staff.getSchoolId()));
    }

    @PostMapping("/prize/scan")
    public AjaxResult scan(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        GkzhSchoolStaff staff = staff(request);
        ensureCanRedeem(staff);
        Long recordId = recordId(body);
        return AjaxResult.success(staffService.scanPrize(recordId, staff.getStaffId(), staff.getSchoolId()));
    }

    @PostMapping("/prize/resolve")
    public AjaxResult resolve(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        GkzhSchoolStaff staff = staff(request);
        ensureCanRedeem(staff);
        Long recordId = body.get("recordId") == null ? null : toLong(body.get("recordId"));
        String code = body.get("redemptionCode") == null ? null : String.valueOf(body.get("redemptionCode"));
        return AjaxResult.success(staffService.resolvePrize(recordId, code, staff.getSchoolId()));
    }

    @PostMapping("/prize/redeem")
    public AjaxResult redeem(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        GkzhSchoolStaff staff = staff(request);
        ensureCanRedeem(staff);
        Long recordId = recordId(body);
        return AjaxResult.success(staffService.redeemPrize(recordId, staff.getStaffId(), staff.getSchoolId(), (String) body.get("remark")));
    }

    @GetMapping("/prize/{recordId}/logs")
    public AjaxResult logs(@PathVariable Long recordId, HttpServletRequest request) {
        GkzhSchoolStaff staff = staff(request);
        ensureCanRedeem(staff);
        StaffPrizeView view = staffService.getPrize(recordId, staff.getSchoolId());
        return AjaxResult.success(staffService.listPrizeLogs(view.getLotteryRecordId()));
    }

    @GetMapping("/statistics/codes")
    public AjaxResult codes(HttpServletRequest request) {
        return AjaxResult.success(staffService.codeStatistics(staff(request).getSchoolId()));
    }

    @GetMapping("/statistics/types")
    public AjaxResult types(HttpServletRequest request) {
        staff(request);
        return AjaxResult.success(hollandCodeService.listCodes());
    }

    @GetMapping("/statistics/activities/{bizType}")
    public AjaxResult activityStatistics(@PathVariable String bizType, HttpServletRequest request) {
        GkzhSchoolStaff staff = staff(request);
        return AjaxResult.success(staffService.activityStatistics(bizType, staff.getSchoolId()));
    }

    @GetMapping("/statistics/games/{gameId}")
    public AjaxResult gameStatistics(@PathVariable Long gameId,
            @RequestParam(required = false) String collegeId,
            @RequestParam(required = false) String majorId,
            @RequestParam(required = false) String gender,
            HttpServletRequest request) {
        GkzhSchoolStaff staff = staff(request);
        return AjaxResult.success(staffService.gameStatistics(gameId, staff.getSchoolId(),
                optionalLong(collegeId), optionalLong(majorId), optionalValue(gender)));
    }

    private GkzhSchoolStaff staff(HttpServletRequest request) {
        Object staff = request.getAttribute("CURRENT_STAFF");
        if (!(staff instanceof StaffSession)) throw new ServiceException("工作人员登录已失效");
        StaffSession session = (StaffSession) staff;
        GkzhSchoolStaff result = new GkzhSchoolStaff();
        result.setStaffId(session.getStaffId());
        result.setSchoolId(session.getSchoolId());
        result.setStaffName(session.getStaffName());
        result.setSchoolName(session.getSchoolName());
        result.setUserName(session.getUserName());
        result.setCanRedeem(session.getCanRedeem());
        return result;
    }

    private void ensureCanRedeem(GkzhSchoolStaff staff) {
        if (!Integer.valueOf(1).equals(staff.getCanRedeem()))
            throw new ServiceException("当前工作人员没有奖品核销权限");
    }

    private Long recordId(Map<String, Object> body) {
        Object value = body.get("recordId");
        if (value == null) throw new ServiceException("缺少中奖记录号");
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new ServiceException("中奖记录号格式错误");
        }
    }

    private Long toLong(Object value) {
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new ServiceException("中奖记录号格式错误");
        }
    }

    private Long optionalLong(String value) {
        String normalized = optionalValue(value);
        if (normalized == null) return null;
        try {
            return Long.valueOf(normalized);
        } catch (NumberFormatException e) {
            throw new ServiceException("筛选参数格式错误");
        }
    }

    private String optionalValue(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty() || "undefined".equalsIgnoreCase(normalized) || "null".equalsIgnoreCase(normalized)) {
            return null;
        }
        return normalized;
    }
}
