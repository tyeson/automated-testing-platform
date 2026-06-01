package com.testplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testplatform.dto.ExecutionDetailVO;
import com.testplatform.entity.Execution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExecutionMapper extends BaseMapper<Execution> {

  IPage<ExecutionDetailVO> selectExecutionDetailPage(
      IPage<ExecutionDetailVO> page,
      @Param("projectId") Long projectId,
      @Param("status") String status);

  ExecutionDetailVO selectExecutionDetailById(@Param("id") Long id);
}
