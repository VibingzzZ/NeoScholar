package com.javaee.backend.service;

import com.javaee.backend.entity.StudentProfile;
import java.util.List;

public interface StudentProfileService {
    StudentProfile getById(Long id);
    List<StudentProfile> listByUserId(Long userId);
    StudentProfile create(StudentProfile profile);
    StudentProfile update(StudentProfile profile);
    StudentProfile getLatestByUserId(Long userId);
}
