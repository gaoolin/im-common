package com.im.qtech.service.msg.util;

import lombok.Getter;

import java.io.Serializable;

/**
 * 响应数据
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/06 13:53:21
 */

@Getter
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private String msg;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ApiResponse(ResponseCode responseCode) {
        this.code = responseCode.getCode();
        this.msg = responseCode.getMessage();
    }

    public ApiResponse(ResponseCode responseCode, T data) {
        this.code = responseCode.getCode();
        this.msg = responseCode.getMessage();
        this.data = data;
    }

    // 常用预设方法
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ResponseCode.SUCCESS);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS, data);
    }

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(ResponseCode.CREATED, data);
    }

    public static <T> ApiResponse<T> accepted(T data) {
        return new ApiResponse<>(ResponseCode.ACCEPTED, data);
    }

    public static <T> ApiResponse<T> noContent() {
        return new ApiResponse<>(ResponseCode.NO_CONTENT);
    }

    public static <T> ApiResponse<T> badRequest(String msg) {
        return new ApiResponse<>(ResponseCode.BAD_REQUEST.getCode(), msg);
    }

    public static <T> ApiResponse<T> unauthorized(String msg) {
        return new ApiResponse<>(ResponseCode.UNAUTHORIZED.getCode(), msg);
    }

    public static <T> ApiResponse<T> forbidden(String msg) {
        return new ApiResponse<>(ResponseCode.FORBIDDEN.getCode(), msg);
    }

    public static <T> ApiResponse<T> notFound(String msg) {
        return new ApiResponse<>(ResponseCode.NOT_FOUND.getCode(), msg);
    }

    public static <T> ApiResponse<T> methodNotAllowed(String msg) {
        return new ApiResponse<>(ResponseCode.METHOD_NOT_ALLOWED.getCode(), msg);
    }

    public static <T> ApiResponse<T> conflict(String msg) {
        return new ApiResponse<>(ResponseCode.CONFLICT.getCode(), msg);
    }

    public static <T> ApiResponse<T> unsupportedMediaType(String msg) {
        return new ApiResponse<>(ResponseCode.UNSUPPORTED_MEDIA_TYPE.getCode(), msg);
    }

    public static <T> ApiResponse<T> internalServerError(String msg) {
        return new ApiResponse<>(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), msg);
    }

    public static <T> ApiResponse<T> notImplemented(String msg) {
        return new ApiResponse<>(ResponseCode.NOT_IMPLEMENTED.getCode(), msg);
    }

    public static <T> ApiResponse<T> badGateway(String msg) {
        return new ApiResponse<>(ResponseCode.BAD_GATEWAY.getCode(), msg);
    }

    public static <T> ApiResponse<T> serviceUnavailable(String msg) {
        return new ApiResponse<>(ResponseCode.SERVICE_UNAVAILABLE.getCode(), msg);
    }

    public static <T> ApiResponse<T> gatewayTimeout(String msg) {
        return new ApiResponse<>(ResponseCode.GATEWAY_TIMEOUT.getCode(), msg);
    }

    public ApiResponse<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public ApiResponse<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public ApiResponse<T> setData(T data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}