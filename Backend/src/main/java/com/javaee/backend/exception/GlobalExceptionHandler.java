package com.javaee.backend.exception;

import com.javaee.backend.config.Result;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;


@Component
public class GlobalExceptionHandler  {

    /**
     * 统一异常处理
     * @param e     错误信息
     * @return      错误结果
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        return Result.errorWithCode(
                StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "操作失败", 500);
    }

}
