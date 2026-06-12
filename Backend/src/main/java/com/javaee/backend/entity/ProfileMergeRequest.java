package com.javaee.backend.entity;

import com.javaee.backend.po.dto.Profile;
import lombok.Data;

@Data
public class ProfileMergeRequest {
    private Profile original;
    private Profile newProfile;
}