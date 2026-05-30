package com.testplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testplatform.entity.ExecutionLog;
import com.testplatform.mapper.ExecutionLogMapper;
import com.testplatform.service.IExecutionLogService;
import org.springframework.stereotype.Service;

@Service
public class ExecutionLogServiceImpl extends ServiceImpl<ExecutionLogMapper, ExecutionLog> implements IExecutionLogService {
}
