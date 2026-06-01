package com.testplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.dto.CreateExecutionRequest;
import com.testplatform.dto.ExecutionDetailVO;
import com.testplatform.dto.ExecutionLogVO;
import com.testplatform.entity.Execution;
import com.testplatform.entity.ExecutionLog;
import com.testplatform.entity.TestCase;
import com.testplatform.entity.TestSuite;
import com.testplatform.mapper.ExecutionMapper;
import com.testplatform.mapper.TestCaseMapper;
import com.testplatform.mapper.TestSuiteMapper;
import com.testplatform.service.IExecutionLogService;
import com.testplatform.service.IExecutionService;
import com.testplatform.service.PlaywrightExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
public class ExecutionController {

  private final IExecutionService executionService;
  private final IExecutionLogService executionLogService;
  private final PlaywrightExecutionService playwrightExecutionService;
  private final ExecutionMapper executionMapper;
  private final TestSuiteMapper testSuiteMapper;
  private final TestCaseMapper testCaseMapper;

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
  public Result<PageResult<ExecutionDetailVO>> list(
      @RequestParam(defaultValue = "1") Long current,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) Long projectId,
      @RequestParam(required = false) String status) {
    Page<ExecutionDetailVO> page = new Page<>(current, size);
    Page<ExecutionDetailVO> result = (Page<ExecutionDetailVO>) executionMapper.selectExecutionDetailPage(page, projectId, status);
    fillDerivedFields(result.getRecords());
    return Result.success(PageResult.from(result));
  }

  @GetMapping("/{id}")
  public Result<ExecutionDetailVO> getById(@PathVariable Long id) {
    ExecutionDetailVO vo = executionMapper.selectExecutionDetailById(id);
    if (vo == null) {
      return Result.error(404, "执行记录不存在");
    }
    fillDerivedFields(List.of(vo));
    List<ExecutionLog> logs = executionLogService.list(
        new LambdaQueryWrapper<ExecutionLog>()
            .eq(ExecutionLog::getExecutionId, id)
            .orderByAsc(ExecutionLog::getStartTime));
    List<ExecutionLogVO> logVOs = enrichLogs(logs);
    vo.setLogs(logVOs);
    return Result.success(vo);
  }

  @PostMapping("/trigger")
  public Result<ExecutionDetailVO> trigger(@RequestBody TriggerRequest request) {
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

    ExecutionDetailVO vo = executionMapper.selectExecutionDetailById(execution.getId());
    if (vo != null) {
      fillDerivedFields(List.of(vo));
    }
    return Result.success(vo);
  }

  @GetMapping("/{id}/logs")
  public Result<List<ExecutionLogVO>> getLogs(@PathVariable Long id) {
    List<ExecutionLog> logs = executionLogService.list(
        new LambdaQueryWrapper<ExecutionLog>()
            .eq(ExecutionLog::getExecutionId, id)
            .orderByAsc(ExecutionLog::getStartTime));
    return Result.success(enrichLogs(logs));
  }

  @GetMapping("/{id}/status")
  public Result<ExecutionDetailVO> getStatus(@PathVariable Long id) {
    ExecutionDetailVO vo = executionMapper.selectExecutionDetailById(id);
    if (vo == null) {
      return Result.error(404, "执行记录不存在");
    }
    fillDerivedFields(List.of(vo));
    return Result.success(vo);
  }

  @PostMapping("/{id}/stop")
  public Result<Void> stop(@PathVariable Long id) {
    playwrightExecutionService.stopExecution(id);
    return Result.success();
  }

  private void fillDerivedFields(List<ExecutionDetailVO> records) {
    if (records == null || records.isEmpty()) {
      return;
    }
    for (ExecutionDetailVO vo : records) {
      if (vo.getStartTime() != null && vo.getEndTime() != null) {
        long seconds = java.time.Duration.between(vo.getStartTime(), vo.getEndTime()).getSeconds();
        vo.setDuration(Math.max(0, seconds));
      } else if (vo.getStartTime() != null) {
        long seconds = java.time.Duration.between(vo.getStartTime(), java.time.LocalDateTime.now()).getSeconds();
        vo.setDuration(Math.max(0, seconds));
      } else {
        vo.setDuration(0L);
      }
      int total = vo.getTotalCases() == null ? 0 : vo.getTotalCases();
      int passed = vo.getPassedCases() == null ? 0 : vo.getPassedCases();
      vo.setPassRate(total > 0 ? (int) Math.round(passed * 100.0 / total) : 0);
    }
  }

  private List<ExecutionLogVO> enrichLogs(List<ExecutionLog> logs) {
    if (logs == null || logs.isEmpty()) {
      return List.of();
    }
    List<Long> caseIds = logs.stream()
        .map(ExecutionLog::getCaseId)
        .filter(id -> id != null)
        .distinct()
        .toList();
    Map<Long, String> caseNameMap = new HashMap<>();
    if (!caseIds.isEmpty()) {
      List<TestCase> cases = testCaseMapper.selectBatchIds(caseIds);
      for (TestCase tc : cases) {
        caseNameMap.put(tc.getId(), tc.getName());
      }
    }
    return logs.stream()
        .map(log -> ExecutionLogVO.from(log, caseNameMap.get(log.getCaseId())))
        .collect(Collectors.toList());
  }

  @lombok.Data
  public static class TriggerRequest {
    private Long projectId;
    private Long suiteId;
    private Long caseId;
    private String triggerType;
  }
}
