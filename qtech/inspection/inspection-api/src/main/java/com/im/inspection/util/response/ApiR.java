package com.im.inspection.util.response;

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
public class ApiR<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    // Predefined successful responses to avoid unnecessary object creation
    private static final ApiR<Void> SUCCESS_NO_DATA = new ApiR<>(ResponseCode.SUCCESS);

    private int code;
    private String msg;
    private T data;

    public ApiR() {
    }

    public ApiR(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ApiR(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ApiR(ResponseCode responseCode) {
        this.code = responseCode.getCode();
        this.msg = responseCode.getMessage();
    }

    public ApiR(ResponseCode responseCode, T data) {
        this.code = responseCode.getCode();
        this.msg = responseCode.getMessage();
        this.data = data;
    }

    /**
     * Returns a generic success response with no message or data.
     *
     * @param <T> Type of data payload
     * @return Successful response instance
     */
    public static <T> ApiR<T> success() {
        @SuppressWarnings("unchecked")
        ApiR<T> result = (ApiR<T>) SUCCESS_NO_DATA;
        return result;
    }

    /**
     * Returns a successful response with given data.
     *
     * @param data Data payload
     * @param <T>  Type of data payload
     * @return Successful response instance
     */
    public static <T> ApiR<T> success(T data) {
        return new ApiR<>(ResponseCode.SUCCESS, data);
    }

    /**
     * Returns a successful response with custom message and data.
     *
     * @param msg  Custom message
     * @param data Data payload
     * @param <T>  Type of data payload
     * @return Successful response instance
     */
    public static <T> ApiR<T> success(String msg, T data) {
        return new ApiR<>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * Returns a CREATED (201) response with data.
     *
     * @param data Data payload
     * @param <T>  Type of data payload
     * @return Created response instance
     */
    public static <T> ApiR<T> created(T data) {
        return new ApiR<>(ResponseCode.CREATED, data);
    }

    /**
     * Returns an ACCEPTED (202) response with data.
     *
     * @param data Data payload
     * @param <T>  Type of data payload
     * @return Accepted response instance
     */
    public static <T> ApiR<T> accepted(T data) {
        return new ApiR<>(ResponseCode.ACCEPTED, data);
    }

    /**
     * Returns a NO CONTENT (204) response.
     *
     * @param <T> Type of data payload
     * @return No content response instance
     */
    public static <T> ApiR<T> noContent() {
        return new ApiR<>(ResponseCode.NO_CONTENT);
    }

    /**
     * Returns a BAD REQUEST (400) response with message.
     *
     * @param msg Error message
     * @param <T> Type of data payload
     * @return Bad request response instance
     */
    public static <T> ApiR<T> badRequest(String msg) {
        return new ApiR<>(ResponseCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * Returns an UNAUTHORIZED (401) response with message.
     *
     * @param msg Error message
     * @param <T> Type of data payload
     * @return Unauthorized response instance
     */
    public static <T> ApiR<T> unauthorized(String msg) {
        return new ApiR<>(ResponseCode.UNAUTHORIZED.getCode(), msg);
    }

    /**
     * Returns a FORBIDDEN (403) response with message.
     *
     * @param msg Error message
     * @param <T> Type of data payload
     * @return Forbidden response instance
     */
    public static <T> ApiR<T> forbidden(String msg) {
        return new ApiR<>(ResponseCode.FORBIDDEN.getCode(), msg);
    }

    /**
     * Returns a NOT FOUND (404) response with message.
     *
     * @param msg Error message
     * @param <T> Type of data payload
     * @return Not found response instance
     */
    public static <T> ApiR<T> notFound(String msg) {
        return new ApiR<>(ResponseCode.NOT_FOUND.getCode(), msg);
    }

    /**
     * Returns a METHOD NOT ALLOWED (405) response with message.
     *
     * @param msg Error message
     * @param <T> Type of data payload
     * @return Method not allowed response instance
     */
    public static <T> ApiR<T> methodNotAllowed(String msg) {
        return new ApiR<>(ResponseCode.METHOD_NOT_ALLOWED.getCode(), msg);
    }

    /**
     * Returns a CONFLICT (409) response with message.
     *
     * @param msg Error message
     * @param <T> Type of data payload
     * @return Conflict response instance
     */
    public static <T> ApiR<T> conflict(String msg) {
        return new ApiR<>(ResponseCode.CONFLICT.getCode(), msg);
    }

    /**
     * Returns an UNSUPPORTED MEDIA TYPE (415) response with message.
     *
     * @param msg Error message
     * @param <T> Type of data payload
     * @return Unsupported media type response instance
     */
    public static <T> ApiR<T> unsupportedMediaType(String msg) {
        return new ApiR<>(ResponseCode.UNSUPPORTED_MEDIA_TYPE.getCode(), msg);
    }

    /**
     * Returns an INTERNAL SERVER ERROR (500) response with message.
     *
     * @param msg Error message
     * @param <T> Type of data payload
     * @return Internal server error response instance
     */
    public static <T> ApiR<T> internalServerError(String msg) {
        return new ApiR<>(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), msg);
    }

    /**
     * Returns a NOT IMPLEMENTED (501) response with message.
     *
     * @param msg Error message
     * @param <T> Type of data payload
     * @return Not implemented response instance
     */
    public static <T> ApiR<T> notImplemented(String msg) {
        return new ApiR<>(ResponseCode.NOT_IMPLEMENTED.getCode(), msg);
    }

    /**
     * Returns a BAD GATEWAY (502) response with message.
     *
     * @param msg Error message
     * @param <T> Type of data payload
     * @return Bad gateway response instance
     */
    public static <T> ApiR<T> badGateway(String msg) {
        return new ApiR<>(ResponseCode.BAD_GATEWAY.getCode(), msg);
    }

    /**
     * Returns a SERVICE UNAVAILABLE (503) response with message.
     *
     * @param msg Error message
     * @param <T> Type of data payload
     * @return Service unavailable response instance
     */
    public static <T> ApiR<T> serviceUnavailable(String msg) {
        return new ApiR<>(ResponseCode.SERVICE_UNAVAILABLE.getCode(), msg);
    }

    /**
     * Returns a GATEWAY TIMEOUT (504) response with message.
     *
     * @param msg Error message
     * @param <T> Type of data payload
     * @return Gateway timeout response instance
     */
    public static <T> ApiR<T> gatewayTimeout(String msg) {
        return new ApiR<>(ResponseCode.GATEWAY_TIMEOUT.getCode(), msg);
    }

    public ApiR<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public ApiR<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public ApiR<T> setData(T data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "ApiR{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
