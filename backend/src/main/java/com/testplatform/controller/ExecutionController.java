package com.testplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.dto.CreateExecutionRequest;
import com.testplatform.entity.Execution;
import com.testplatform.entity.ExecutionLog;
import com.testplatform.service.IExecutionLogService;
import com.testplatform.service.IExecutionService;
import com.testplatform.service.PlaywrightExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
public class ExecutionController {

  private final IExecutionService executionService;
  private final IExecutionLogService executionLogService;
  private final PlaywrightExecutionService playwrightExecutionService;

  @PostMapping
  public Result<Execution> create(@Valid @RequestBody CreateExecutionRequest request) {
    Execution execution = executionService.createExecution(
        request.getProjectId(),
        request.getSuiteId(),
        request.getCaseId(),
        request.getTriggerType());
    return Result.success(execution);
  }

  @GetMapping
  public Result<PageResult<Execution>> list(
      @RequestParam(defaultValue = "1") Long current,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) Long projectId,
      @RequestParam(required = false) String status) {
    Page<Execution> page = new Page<>(current, size);
    LambdaQueryWrapper<Execution> wrapper = new LambdaQueryWrapper<>();
    if (projectId != null) {
      wrapper.eq(Execution::getProjectId, projectId);
    }
    if (status != null && !status.isEmpty()) {
      wrapper.eq(Execution::getStatus, status);
    }
    wrapper.orderByDesc(Execution::getCreateTime);
    return Result.success(PageResult.from(executionService.page(page, wrapper)));
  }

  @GetMapping("/{id}")
  public Result<Execution> getById(@PathVariable Long id) {
    return Result.success(executionService.getById(id));
  }

  @PostMapping("/trigger")
  public Result<Execution> trigger(@RequestBody TriggerRequest request) {
    Execution execution = executionService.createExecution(
        request.getProjectId(),
        request.getSuiteId(),
        request.getCaseId(),
        request.getTriggerType());

    if (request.getCaseId() != null) {
      playwrightExecutionService.executeCase(request.getCaseId(), execution.getId());
    } else if (request.getSuiteId() != null) {
      playwrightExecutionService.executeSuite(request.getSuiteId(), execution.getId());
    }

    return Result.success(executionService.getById(execution.getId()));
  }

  @GetMapping("/{id}/logs")
  public Result<List<ExecutionLog>> getLogs(@PathVariable Long id) {
    List<ExecutionLog> logs = executionLogService.list(
        new LambdaQueryWrapper<ExecutionLog>()
            .eq(ExecutionLog::getExecutionId, id)
            .orderByAsc(ExecutionLog::getStartTime));
    return Result.success(logs);
  }

  @GetMapping("/{id}/status")
  public Result<Execution> getStatus(@PathVariable Long id) {
    return Result.success(executionService.getById(id));
  }

  @PostMapping("/{id}/stop")
  public Result<Void> stop(@PathVariable Long id) {
    playwrightExecutionService.stopExecution(id);
    return Result.success();
  }

  @lombok.Data
  public static class TriggerRequest {
    private Long projectId;
    private Long suiteId;
    private Long caseId;
    private String triggerType;
  }
}
