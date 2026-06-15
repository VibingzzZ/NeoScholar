package com.javaee.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileStorageServiceTest {

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    void testSaveAndGet() throws IOException {
        System.out.println("=== 测试文件存储与读取 ===");

        String content = "这是微积分基础的PPT大纲\n一、极限概念\n二、导数定义\n三、求导法则";
        String fileName = UUID.randomUUID() + "_微积分基础.txt";

        String filePath = fileStorageService.save(content, fileName);
        System.out.println("文件保存路径: " + filePath);

        Path savedPath = Paths.get(filePath);
        assertTrue(Files.exists(savedPath), "文件应该存在");
        String readContent = Files.readString(savedPath);
        assertEquals(content, readContent);

        Files.deleteIfExists(savedPath);
        System.out.println("文件读取内容与原始一致");
    }

    @Test
    void testSaveMultipleFiles() throws IOException {
        System.out.println("=== 测试多文件存储（同名防冲突） ===");

        String name = "test.txt";
        String path1 = fileStorageService.save("内容A", name);
        String path2 = fileStorageService.save("内容B", name);

        assertNotEquals(path1, path2, "同名文件不应该覆盖");
        System.out.println("路径1: " + path1);
        System.out.println("路径2: " + path2);

        Files.deleteIfExists(Paths.get(path1));
        Files.deleteIfExists(Paths.get(path2));
    }
}