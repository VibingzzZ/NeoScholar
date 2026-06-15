package com.javaee.backend.service;



import java.io.IOException;


public interface FileStorageService {

    String save(String content,String fileName) throws IOException;


}
