package com.testplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.entity.ExecutionLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExecutionLogMapper extends BaseMapper<ExecutionLog> {
}
