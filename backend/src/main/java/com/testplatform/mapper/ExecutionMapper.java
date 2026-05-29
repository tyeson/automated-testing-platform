package com.testplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.entity.Execution;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExecutionMapper extends BaseMapper<Execution> {
}
