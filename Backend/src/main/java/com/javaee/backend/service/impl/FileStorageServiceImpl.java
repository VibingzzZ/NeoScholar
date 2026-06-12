package com.javaee.backend.service.impl;


import com.javaee.backend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.storage.path:./upload}")
    private String uploadDir;
    @Override
    public String save(String content, String fileName) throws IOException {
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);

        String uniqueName = UUID.randomUUID()+"_"+fileName;
        Path filePath = dir.resolve(uniqueName);

        Files.writeString(filePath,content);

        return filePath.toString();
    }
}
