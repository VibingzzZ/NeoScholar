package com.javaee.backend.service;

import java.io.IOException;

public interface ResourceOrchestratorService {
    void generateResource(Long pathId) throws IOException;
}
