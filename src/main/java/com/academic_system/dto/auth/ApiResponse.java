package com.academic_system.dto.auth;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Boolean requiresPasswordChange;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data, Boolean requiresPasswordChange) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.requiresPasswordChange = requiresPasswordChange;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data, false);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, false);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, false);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public Boolean getRequiresPasswordChange() { return requiresPasswordChange; }
    public void setRequiresPasswordChange(Boolean requiresPasswordChange) { this.requiresPasswordChange = requiresPasswordChange; }
}