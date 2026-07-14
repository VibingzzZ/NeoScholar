package com.javaee.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javaee.backend.entity.ActivityLog;
import com.javaee.backend.mapper.ActivityLogMapper;
import com.javaee.backend.service.ActivityLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    @Autowired
    private ActivityLogMapper activityLogMapper;

    @Override
    public void incrementActivity(Long userId) {
        LocalDate today = LocalDate.now();

        LambdaQueryWrapper<ActivityLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityLog::getUserId, userId)
                .eq(ActivityLog::getActivityDate, today);
        ActivityLog existing = activityLogMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setCount(existing.getCount() + 1);
            activityLogMapper.updateById(existing);
            log.debug("活动计数 +1: userId={}, date={}, count={}", userId, today, existing.getCount());
        } else {
            ActivityLog newLog = new ActivityLog();
            newLog.setUserId(userId);
            newLog.setActivityDate(today);
            newLog.setCount(1);
            activityLogMapper.insert(newLog);
            log.debug("新增活动记录: userId={}, date={}, count=1", userId, today);
        }
    }
}
