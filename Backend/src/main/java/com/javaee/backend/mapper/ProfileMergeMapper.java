package com.javaee.backend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaee.backend.entity.StudentProfile;
import org.apache.ibatis.annotations.Mapper;




@Mapper
public interface ProfileMergeMapper extends BaseMapper<StudentProfile> {

}
