package com.javaee.backend.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数不合法
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException e, WebRequest request, HttpServletResponse response) throws IOException {
        log.warn("请求参数异常: {}", e.getMessage());
        return writeError(response, request, "请求参数错误: " + e.getMessage());
    }

    /**
     * 空指针 — 不把具体信息暴露给前端
     */
    @ExceptionHandler(NullPointerException.class)
    public Object handleNullPointer(NullPointerException e, WebRequest request, HttpServletResponse response) throws IOException {
        log.error("空指针异常: ", e);
        return writeError(response, request, "服务内部错误，请稍后重试");
    }

    /**
     * 兜底：所有未单独处理的异常。
     * 如果是 SSE 流式请求，不能返回 JSON（Content-Type 已锁定为 text/event-stream），
     * 需要直接写 SSE 格式的错误事件到 response。
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, WebRequest request, HttpServletResponse response) throws IOException {
        log.error("系统异常: ", e);
        return writeError(response, request, "系统错误: " + e.getMessage());
    }

    /** 根据请求类型返回 JSON 或 SSE 格式的错误 */
    private Object writeError(HttpServletResponse response, WebRequest request, String message) throws IOException {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            // SSE 流式请求：直接写 SSE 事件，不经过 MessageConverter
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("data: " + message + "\n\n");
            response.getWriter().write("data: [DONE]\n\n");
            response.getWriter().flush();
            return null; // 已手动处理
        }
        // 普通请求：返回 JSON
        return Result.error(message);
    }
}