package com.gkzh.app.core.config;

import com.gkzh.common.constant.HttpStatus;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.exception.user.StuAuthException;
import com.gkzh.common.utils.MessageUtils;
import com.gkzh.qdqt.service.StuTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class StudentAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private StuTokenService stuTokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        StudentCheckin studentCheckin = stuTokenService.getStudentCheckin(request);


        if (studentCheckin == null || studentCheckin.getToken() == null || !stuTokenService.validateToken(studentCheckin)) {
            throw new StuAuthException(HttpStatus.UNAUTHORIZED,MessageUtils.message("student.not.token"));
        }
        request.setAttribute("CURRENT_STUDENT", studentCheckin);
        return true;
    }
}
