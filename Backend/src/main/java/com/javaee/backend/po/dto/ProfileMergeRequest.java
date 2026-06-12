package com.javaee.backend.po.dto;

import lombok.Data;

@Data
public class ProfileMergeRequest {
    private Profile original;
    private Profile newProfile;
}