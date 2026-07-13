package com.javaee.backend.po.dto;

import com.javaee.backend.entity.Profile;
import lombok.Data;

@Data
public class ProfileMergeRequest {
    private Profile original;
    private Profile newProfile;
}