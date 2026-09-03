package com.gkzh.app.core.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.gkzh.activity.domain.staff.GkzhSchoolStaff;
import com.gkzh.app.core.staff.StaffSession;
import com.gkzh.app.core.staff.StaffTokenService;
import com.gkzh.common.constant.HttpStatus;
import com.gkzh.common.exception.user.StuAuthException;

@Component
public class StaffAuthInterceptor implements HandlerInterceptor {
    @Autowired private StaffTokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        StaffSession staff = tokenService.getSession(request);
        if (staff == null) throw new StuAuthException(HttpStatus.UNAUTHORIZED, "工作人员登录已失效");
        request.setAttribute("CURRENT_STAFF", staff);
        return true;
    }
}
