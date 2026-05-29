package com.testplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.dto.CreateExecutionRequest;
import com.testplatform.entity.Execution;
import com.testplatform.service.IExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
public class ExecutionController {

  private final IExecutionService executionService;

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
}
