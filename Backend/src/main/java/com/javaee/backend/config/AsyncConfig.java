package com.javaee.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean("profileMergeExecutor")
    public ExecutorService profileMergeExecutor() {
        return Executors.newFixedThreadPool(5);
    }

    @Bean("resourceGenExecutor")
    public ExecutorService resourceGenExecutor() {
        return Executors.newFixedThreadPool(3);
    }

}
