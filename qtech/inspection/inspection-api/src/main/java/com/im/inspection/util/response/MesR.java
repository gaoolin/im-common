package com.im.inspection.util.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MES响应信息
 * <p>
 * 此响应信息主体，专门用于返回数据给MES
 * 仅对MES相应接口的数据格式做适配
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/13 17:24:23
 */
public class MesR<T> extends ApiR<T> {

    @JsonProperty("Code")
    private int code;

    @JsonProperty("Msg")
    private String msg;

    private T data;

    @Override
    public int getCode() {
        return code;
    }

    // 实现链式调用
    @Override
    public MesR<T> setCode(int code) {
        this.code = code;
        return this;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public MesR<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public MesR<T> setData(T data) {
        this.data = data;
        return this;
    }
}