package com.javaee.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.javaee.backend.entity.StudentProfile;
import com.javaee.backend.mapper.ProfileMergeMapper;
import com.javaee.backend.service.StudentProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Service
public class StudentProfileServiceImpl extends ServiceImpl<ProfileMergeMapper, StudentProfile> implements StudentProfileService {

    @Override
    public StudentProfile getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<StudentProfile> listByUserId(Long userId) {
        LambdaQueryWrapper<StudentProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentProfile::getUserId, userId)
                .orderByDesc(StudentProfile::getUpdateAt);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public StudentProfile create(StudentProfile profile) {
        profile.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        baseMapper.insert(profile);
        log.info("创建学生画像成功, ID: {}", profile.getId());
        return profile;
    }

    @Override
    public StudentProfile update(StudentProfile profile) {
        profile.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        baseMapper.updateById(profile);
        log.info("更新学生画像成功, ID: {}", profile.getId());
        return profile;
    }

    @Override
    public StudentProfile getLatestByUserId(Long userId) {
        LambdaQueryWrapper<StudentProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentProfile::getUserId, userId)
                .orderByDesc(StudentProfile::getUpdateAt)
                .last("LIMIT 1");
        return baseMapper.selectOne(wrapper);
    }
}
