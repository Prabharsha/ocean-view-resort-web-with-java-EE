package com.oceanview.util;

public class ServiceResult {
    private final boolean success;
    private final String message;
    private Object data;

    private ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ServiceResult success(String msg) {
        return new ServiceResult(true, msg);
    }

    public static ServiceResult success(String msg, Object data) {
        ServiceResult r = success(msg);
        r.data = data;
        return r;
    }

    public static ServiceResult failure(String msg) {
        return new ServiceResult(false, msg);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}

