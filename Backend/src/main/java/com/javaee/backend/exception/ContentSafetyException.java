package com.javaee.backend.exception;

import lombok.Getter;

/**
 * 内容安全审核不通过时抛出的异常
 */
@Getter
public class ContentSafetyException extends RuntimeException {

    /** 违规类别 */
    private final String category;

    public ContentSafetyException(String message, String category) {
        super(message);
        this.category = category;
    }

    public ContentSafetyException(String message) {
        super(message);
        this.category = "未知";
    }
}
