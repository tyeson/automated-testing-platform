package com.testplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.entity.Report;
import com.testplatform.mapper.ReportMapper;
import com.testplatform.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

  private final IReportService reportService;
  private final ReportMapper reportMapper;

  @GetMapping
  public Result<PageResult<Report>> list(
      @RequestParam(defaultValue = "1") Long current,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) Long executionId) {
    Page<Report> page = new Page<>(current, size);
    LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
    if (executionId != null) {
      wrapper.eq(Report::getExecutionId, executionId);
    }
    wrapper.orderByDesc(Report::getCreateTime);
    return Result.success(PageResult.from(reportService.page(page, wrapper)));
  }

  @GetMapping("/dashboard")
  public Result<Map<String, Object>> getDashboardMetrics() {
    Map<String, Object> metrics = new HashMap<>();
    long totalExecutions = reportMapper.countAllExecutions();
    long successCount = reportMapper.countByStatus("SUCCESS");
    long failCount = reportMapper.countByStatus("FAILED");
    long projectCount = reportMapper.countProjects();
    long runningCount = reportMapper.countByStatus("RUNNING");
    long pendingCount = reportMapper.countByStatus("PENDING");

    double successRate = totalExecutions > 0
        ? roundTo1Decimal((double) successCount * 100 / totalExecutions)
        : 0.0;
    double failRate = totalExecutions > 0
        ? roundTo1Decimal((double) failCount * 100 / totalExecutions)
        : 0.0;

    metrics.put("todayExecutions", totalExecutions);
    metrics.put("successCount", successCount);
    metrics.put("failCount", failCount);
    metrics.put("successRate", successRate);
    metrics.put("failRate", failRate);
    metrics.put("projectCount", projectCount);
    metrics.put("runningCount", runningCount);
    metrics.put("pendingCount", pendingCount);
    return Result.success(metrics);
  }

  @GetMapping("/trend")
  public Result<List<Map<String, Object>>> getTrendData(@RequestParam(defaultValue = "7") int days) {
    int safeDays = Math.max(1, Math.min(days, 30));
    List<Map<String, Object>> trendData = reportMapper.fillMissingTrendDays(safeDays);
    for (Map<String, Object> day : trendData) {
      long total = toLong(day.get("total"));
      long passed = toLong(day.get("passed"));
      long failed = toLong(day.get("failed"));
      int passRate = total > 0
          ? (int) Math.round(passed * 100.0 / total)
          : 0;
      day.put("passRate", passRate);
    }
    return Result.success(trendData);
  }

  @GetMapping("/environment")
  public Result<List<Map<String, Object>>> getEnvironmentAnalysis() {
    List<Map<String, Object>> rows = reportMapper.environmentStats();
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("environment", row.get("environment"));
      long total = toLong(row.get("totalExecutions"));
      long success = toLong(row.get("successCount"));
      double successRate = total > 0
          ? roundTo1Decimal(success * 100.0 / total)
          : 0.0;
      item.put("totalExecutions", total);
      item.put("successCount", success);
      item.put("successRate", successRate);
      result.add(item);
    }
    return Result.success(result);
  }

  @GetMapping("/failure")
  public Result<List<Map<String, Object>>> getFailureAnalysis(@RequestParam(required = false) Long projectId) {
    List<Map<String, Object>> rows = reportMapper.failureReasons();
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("reason", row.get("reason"));
      item.put("count", toLong(row.get("count")));
      result.add(item);
    }
    return Result.success(result);
  }

  @GetMapping("/ranking")
  public Result<List<Map<String, Object>>> getProjectExecutionRanking() {
    List<Map<String, Object>> rows = reportMapper.projectExecutionRanking();
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("projectId", row.get("projectId"));
      item.put("projectName", row.get("projectName"));
      item.put("totalExecutions", toLong(row.get("totalExecutions")));
      result.add(item);
    }
    return Result.success(result);
  }

  @GetMapping("/status-distribution")
  public Result<List<Map<String, Object>>> getStatusDistribution() {
    List<Map<String, Object>> rows = reportMapper.statusDistribution();
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("status", row.get("status"));
      item.put("count", toLong(row.get("count")));
      result.add(item);
    }
    return Result.success(result);
  }

  @GetMapping("/list")
  public Result<PageResult<Report>> getReportList(
      @RequestParam(defaultValue = "1") Long page,
      @RequestParam(defaultValue = "10") Long pageSize) {
    Page<Report> reportPage = new Page<>(page, pageSize);
    LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
    wrapper.orderByDesc(Report::getCreateTime);
    return Result.success(PageResult.from(reportService.page(reportPage, wrapper)));
  }

  @GetMapping("/{id}")
  public Result<Report> getById(@PathVariable Long id) {
    return Result.success(reportService.getById(id));
  }

  @PostMapping
  public Result<Void> create(@RequestBody Report report) {
    reportService.save(report);
    return Result.success();
  }

  @PutMapping("/{id}")
  public Result<Void> update(@PathVariable Long id, @RequestBody Report report) {
    report.setId(id);
    reportService.updateById(report);
    return Result.success();
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    reportService.removeById(id);
    return Result.success();
  }

  private static long toLong(Object o) {
    if (o == null) return 0L;
    if (o instanceof Number n) return n.longValue();
    try {
      return Long.parseLong(o.toString());
    } catch (NumberFormatException ex) {
      return 0L;
    }
  }

  private static double roundTo1Decimal(double v) {
    return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
  }
}
