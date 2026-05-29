package com.testplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.testplatform.entity.Execution;

public interface IExecutionService extends IService<Execution> {
  Execution createExecution(Long projectId, Long suiteId, Long caseId, String triggerType);
}
