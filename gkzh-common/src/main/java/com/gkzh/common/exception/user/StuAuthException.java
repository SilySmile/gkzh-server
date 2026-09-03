package com.gkzh.common.exception.user;

public class StuAuthException extends RuntimeException{
    private final int code;

    public StuAuthException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
