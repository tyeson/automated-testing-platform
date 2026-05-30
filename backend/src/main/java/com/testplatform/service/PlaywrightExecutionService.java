package com.testplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.entity.*;
import com.testplatform.mapper.SuiteCaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaywrightExecutionService {

  private final IExecutionService executionService;
  private final IExecutionLogService executionLogService;
  private final ITestCaseService testCaseService;
  private final SuiteCaseMapper suiteCaseMapper;

  private final ConcurrentHashMap<Long, Process> runningProcesses = new ConcurrentHashMap<>();

  public Execution executeCase(Long caseId, Long executionId) {
    TestCase testCase = testCaseService.getById(caseId);
    if (testCase == null) {
      throw new RuntimeException("测试用例不存在");
    }

    Execution execution = executionService.getById(executionId);
    if (execution == null) {
      throw new RuntimeException("执行记录不存在");
    }

    execution.setStatus("RUNNING");
    execution.setStartTime(LocalDateTime.now());
    executionService.updateById(execution);

    ExecutionLog executionLog = new ExecutionLog();
    executionLog.setExecutionId(executionId);
    executionLog.setCaseId(caseId);
    executionLog.setStatus("RUNNING");
    executionLog.setStartTime(LocalDateTime.now());
    executionLogService.save(executionLog);

    try {
      String gitPath = testCase.getGitPath();
      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command("npx", "playwright", "test", gitPath);
      processBuilder.redirectErrorStream(true);

      Process process = processBuilder.start();
      runningProcesses.put(executionId, process);

      StringBuilder consoleOutput = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          consoleOutput.append(line).append("\n");
        }
      }

      int exitCode = process.waitFor();
      runningProcesses.remove(executionId);

      String status = exitCode == 0 ? "PASSED" : "FAILED";
      executionLog.setStatus(status);
      executionLog.setConsoleLog(consoleOutput.toString());
      executionLog.setEndTime(LocalDateTime.now());

      collectArtifacts(executionLog, caseId, executionId);

      executionLogService.updateById(executionLog);

      execution.setStatus("COMPLETED");
      execution.setEndTime(LocalDateTime.now());
      execution.setTotalCount(1);
      if (exitCode == 0) {
        execution.setPassCount(1);
        execution.setFailCount(0);
      } else {
        execution.setPassCount(0);
        execution.setFailCount(1);
      }
      executionService.updateById(execution);

    } catch (Exception e) {
      log.error("执行用例失败: caseId={}, executionId={}", caseId, executionId, e);
      executionLog.setStatus("ERROR");
      executionLog.setErrorMessage(e.getMessage());
      executionLog.setEndTime(LocalDateTime.now());
      executionLogService.updateById(executionLog);

      execution.setStatus("ERROR");
      execution.setEndTime(LocalDateTime.now());
      execution.setTotalCount(1);
      execution.setPassCount(0);
      execution.setFailCount(1);
      executionService.updateById(execution);
    }

    return execution;
  }

  public Execution executeSuite(Long suiteId, Long executionId) {
    List<SuiteCase> suiteCases = suiteCaseMapper.selectList(
        new LambdaQueryWrapper<SuiteCase>().eq(SuiteCase::getSuiteId, suiteId));

    if (suiteCases.isEmpty()) {
      Execution execution = executionService.getById(executionId);
      execution.setStatus("COMPLETED");
      execution.setEndTime(LocalDateTime.now());
      execution.setTotalCount(0);
      execution.setPassCount(0);
      execution.setFailCount(0);
      executionService.updateById(execution);
      return execution;
    }

    Execution execution = executionService.getById(executionId);
    execution.setStatus("RUNNING");
    execution.setStartTime(LocalDateTime.now());
    execution.setTotalCount(suiteCases.size());
    executionService.updateById(execution);

    int passCount = 0;
    int failCount = 0;

    for (SuiteCase suiteCase : suiteCases) {
      ExecutionLog executionLog = new ExecutionLog();
      executionLog.setExecutionId(executionId);
      executionLog.setCaseId(suiteCase.getCaseId());
      executionLog.setStatus("RUNNING");
      executionLog.setStartTime(LocalDateTime.now());
      executionLogService.save(executionLog);

      try {
        TestCase testCase = testCaseService.getById(suiteCase.getCaseId());
        String gitPath = testCase.getGitPath();

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("npx", "playwright", "test", gitPath);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        runningProcesses.put(executionId, process);

        StringBuilder consoleOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
          String line;
          while ((line = reader.readLine()) != null) {
            consoleOutput.append(line).append("\n");
          }
        }

        int exitCode = process.waitFor();
        runningProcesses.remove(executionId);

        String status = exitCode == 0 ? "PASSED" : "FAILED";
        executionLog.setStatus(status);
        executionLog.setConsoleLog(consoleOutput.toString());
        executionLog.setEndTime(LocalDateTime.now());

        collectArtifacts(executionLog, suiteCase.getCaseId(), executionId);

        executionLogService.updateById(executionLog);

        if (exitCode == 0) {
          passCount++;
        } else {
          failCount++;
        }

      } catch (Exception e) {
        log.error("执行用例失败: caseId={}, executionId={}", suiteCase.getCaseId(), executionId, e);
        executionLog.setStatus("ERROR");
        executionLog.setErrorMessage(e.getMessage());
        executionLog.setEndTime(LocalDateTime.now());
        executionLogService.updateById(executionLog);
        failCount++;
      }
    }

    execution.setStatus("COMPLETED");
    execution.setEndTime(LocalDateTime.now());
    execution.setPassCount(passCount);
    execution.setFailCount(failCount);
    executionService.updateById(execution);

    return execution;
  }

  public void stopExecution(Long executionId) {
    Process process = runningProcesses.remove(executionId);
    if (process != null && process.isAlive()) {
      process.destroyForcibly();
    }

    Execution execution = executionService.getById(executionId);
    if (execution != null && "RUNNING".equals(execution.getStatus())) {
      execution.setStatus("STOPPED");
      execution.setEndTime(LocalDateTime.now());
      executionService.updateById(execution);
    }

    List<ExecutionLog> runningLogs = executionLogService.list(
        new LambdaQueryWrapper<ExecutionLog>()
            .eq(ExecutionLog::getExecutionId, executionId)
            .eq(ExecutionLog::getStatus, "RUNNING"));
    for (ExecutionLog logEntry : runningLogs) {
      logEntry.setStatus("STOPPED");
      logEntry.setEndTime(LocalDateTime.now());
      executionLogService.updateById(logEntry);
    }
  }

  private void collectArtifacts(ExecutionLog executionLog, Long caseId, Long executionId) {
    try {
      Path screenshotDir = Path.of("artifacts", String.valueOf(executionId), "screenshots");
      if (Files.exists(screenshotDir)) {
        List<String> screenshots = Files.list(screenshotDir)
            .filter(p -> p.toString().endsWith(".png"))
            .map(Path::toString)
            .collect(Collectors.toList());
        if (!screenshots.isEmpty()) {
          executionLog.setScreenshotPath(String.join(",", screenshots));
        }
      }

      Path videoDir = Path.of("artifacts", String.valueOf(executionId), "videos");
      if (Files.exists(videoDir)) {
        List<String> videos = Files.list(videoDir)
            .filter(p -> p.toString().endsWith(".webm"))
            .map(Path::toString)
            .collect(Collectors.toList());
        if (!videos.isEmpty()) {
          executionLog.setVideoPath(String.join(",", videos));
        }
      }
    } catch (Exception e) {
      log.warn("收集artifacts失败: caseId={}, executionId={}", caseId, executionId, e);
    }
  }
}
