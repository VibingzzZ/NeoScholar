package com.javaee.backend.service;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {

    String save(String content, String fileName) throws IOException;

    /**
     * 读取文件内容，返回文件路径（用于下载）
     */
    Path getFilePath(String filePath);
}
