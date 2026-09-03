package com.gkzh.common.exception.user;

/**
 * 用户不存在异常类
 * 
 *
 */
public class StuNotExistsException extends UserException
{
    private static final long serialVersionUID = 1L;

    public StuNotExistsException()
    {
        super("student.not.exists", null);
    }
}
