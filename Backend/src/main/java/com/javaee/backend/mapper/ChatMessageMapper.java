package com.javaee.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaee.backend.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
