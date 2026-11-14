package com.heritage.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回封装对象
 * 结构：{ "code": 200, "msg": "提示信息", "data": {} }
 */
@Data
public class Result<T> implements Serializable {

    /** 状态码 */
    private Integer code;
    /** 提示信息 */
    private String msg;
    /** 业务数据 */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /** 成功（带数据，默认提示"操作成功"） */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /** 成功（自定义提示 + 数据） */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    /** 失败（默认 500） */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(ResultCode.ERROR.getCode(), msg, null);
    }

    /** 失败（自定义状态码 + 提示） */
    public static <T> Result<T> fail(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    /** 失败（使用 ResultCode 枚举） */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }
}