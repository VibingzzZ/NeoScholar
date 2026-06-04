package com.javaee.backend.config;

public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public Result<T> success(T data) {
        this.code = 200;
        this.message = "success";
        this.data = data;
        return this;
    }

    private Result(){}
    public Result <T> success(){
        this.code = 200;
        this.message = "success";
        return this;
    }

    public Result<T> error(String message) {
        this.code = 500;
        this.message = message;
        return this;
    }

    /**
     * 失败返回结果（带错误码）
     *
     * @param message 提示信息
     * @param code 错误码
     */
    public static <T> Result<T> errorWithCode(String message, Integer code) {
        Result<T> result = new Result<>();
        result.message = message;
        result.code = code;
        return result;
    }

}


