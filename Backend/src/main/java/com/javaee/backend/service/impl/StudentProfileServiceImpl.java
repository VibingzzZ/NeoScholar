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
                .orderByDesc(StudentProfile::getIsActive)
                .orderByDesc(StudentProfile::getUpdateAt);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public StudentProfile create(StudentProfile profile) {
        profile.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        if (profile.getSource() == null) {
            profile.setSource("manual");
        }
        // 如果是用户第一条画像，自动设为活跃
        long count = baseMapper.selectCount(
                new LambdaQueryWrapper<StudentProfile>().eq(StudentProfile::getUserId, profile.getUserId()));
        if (count == 0) {
            profile.setIsActive(true);
        } else {
            profile.setIsActive(profile.getIsActive() != null && profile.getIsActive());
        }
        baseMapper.insert(profile);
        log.info("创建学生画像成功, ID: {}, 活跃: {}", profile.getId(), profile.getIsActive());
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

    @Override
    public StudentProfile getActiveByUserId(Long userId) {
        LambdaQueryWrapper<StudentProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentProfile::getUserId, userId)
                .eq(StudentProfile::getIsActive, true)
                .last("LIMIT 1");
        StudentProfile active = baseMapper.selectOne(wrapper);
        // 如果没有活跃画像，回退到最新画像
        if (active == null) {
            active = getLatestByUserId(userId);
            if (active != null) {
                active.setIsActive(true);
                baseMapper.updateById(active);
                log.info("自动将最新画像设为活跃, ID: {}", active.getId());
            }
        }
        return active;
    }

    @Override
    public void setActive(Long profileId, Long userId) {
        // 先将该用户所有画像设为非活跃
        LambdaQueryWrapper<StudentProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentProfile::getUserId, userId)
                .eq(StudentProfile::getIsActive, true);
        List<StudentProfile> activeList = baseMapper.selectList(wrapper);
        for (StudentProfile p : activeList) {
            p.setIsActive(false);
            baseMapper.updateById(p);
        }
        // 设置指定画像为活跃
        StudentProfile profile = baseMapper.selectById(profileId);
        if (profile != null && profile.getUserId().equals(userId)) {
            profile.setIsActive(true);
            profile.setUpdateAt(new Timestamp(System.currentTimeMillis()));
            baseMapper.updateById(profile);
            log.info("切换活跃画像, userId: {}, profileId: {}", userId, profileId);
        }
    }
}
