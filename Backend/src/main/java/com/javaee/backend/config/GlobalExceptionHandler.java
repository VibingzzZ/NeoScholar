package com.javaee.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数不合法
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("请求参数异常: {}", e.getMessage());
        return Result.error("请求参数错误: " + e.getMessage());
    }

    /**
     * 空指针 — 不把具体信息暴露给前端
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<String> handleNullPointer(NullPointerException e) {
        log.error("空指针异常: ", e);
        return Result.error("服务内部错误，请稍后重试");
    }

    /**
     * 兜底：所有未单独处理的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error("系统错误: " + e.getMessage());
    }
}