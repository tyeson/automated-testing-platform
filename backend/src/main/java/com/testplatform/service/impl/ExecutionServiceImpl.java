package com.testplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testplatform.entity.Execution;
import com.testplatform.mapper.ExecutionMapper;
import com.testplatform.service.IExecutionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExecutionServiceImpl extends ServiceImpl<ExecutionMapper, Execution> implements IExecutionService {

  @Override
  public Execution createExecution(Long projectId, Long suiteId, Long caseId, String triggerType) {
    Execution execution = new Execution();
    execution.setProjectId(projectId);
    execution.setSuiteId(suiteId);
    execution.setCaseId(caseId);
    execution.setTriggerType(triggerType);
    execution.setStatus("PENDING");
    execution.setStartTime(LocalDateTime.now());
    execution.setTotalCount(0);
    execution.setPassCount(0);
    execution.setFailCount(0);
    save(execution);
    return execution;
  }
}
