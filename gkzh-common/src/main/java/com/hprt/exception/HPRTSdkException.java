package com.hprt.exception;

import java.io.Serial;

public class HPRTSdkException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public HPRTSdkException(String msg) {
        super(msg);
    }

    public HPRTSdkException(String msg, Throwable e) {
        super(msg, e);
    }
}
