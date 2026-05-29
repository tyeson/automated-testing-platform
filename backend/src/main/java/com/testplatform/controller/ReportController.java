package com.testplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.entity.Execution;
import com.testplatform.entity.Report;
import com.testplatform.service.IExecutionService;
import com.testplatform.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

  private final IReportService reportService;
  private final IExecutionService executionService;

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
    long totalExecutions = executionService.count();
    long successCount = executionService.count(new LambdaQueryWrapper<Execution>().eq(Execution::getStatus, "SUCCESS"));
    long failCount = executionService.count(new LambdaQueryWrapper<Execution>().eq(Execution::getStatus, "FAILED"));
    double successRate = totalExecutions > 0 ? (double) successCount / totalExecutions * 100 : 0;
    double failRate = totalExecutions > 0 ? (double) failCount / totalExecutions * 100 : 0;
    metrics.put("todayExecutions", totalExecutions);
    metrics.put("successRate", Math.round(successRate * 10.0) / 10.0);
    metrics.put("failRate", Math.round(failRate * 10.0) / 10.0);
    metrics.put("projectCount", 0);
    return Result.success(metrics);
  }

@GetMapping("/trend")
  public Result<List<Map<String, Object>>> getTrendData(@RequestParam(defaultValue = "7") int days) {
    List<Map<String, Object>> trendData = new ArrayList<>();
    Calendar cal = Calendar.getInstance();
    for (int i = days - 1; i >= 0; i--) {
      cal.setTime(new Date());
      cal.add(Calendar.DAY_OF_MONTH, -i);
      String date = String.format("%02d/%02d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
      Map<String, Object> dayData = new HashMap<>();
      dayData.put("date", date);
      dayData.put("total", (int) (Math.random() * 100) + 50);
      dayData.put("passed", (int) (Math.random() * 80) + 40);
      dayData.put("failed", (int) (Math.random() * 20) + 5);
      dayData.put("passRate", (int) (Math.random() * 20) + 80);
      trendData.add(dayData);
    }
    return Result.success(trendData);
  }

  @GetMapping("/environment")
  public Result<List<Map<String, Object>>> getEnvironmentAnalysis() {
    List<Map<String, Object>> data = new ArrayList<>();
    String[] envs = { "DEV", "TEST", "UAT", "PROD" };
    for (String env : envs) {
      Map<String, Object> item = new HashMap<>();
      item.put("environment", env);
      item.put("successRate", (int) (Math.random() * 20) + 80);
      item.put("totalExecutions", (int) (Math.random() * 500) + 100);
      data.add(item);
    }
    return Result.success(data);
  }

  @GetMapping("/failure")
  public Result<List<Map<String, Object>>> getFailureAnalysis(@RequestParam(required = false) Long projectId) {
    List<Map<String, Object>> data = new ArrayList<>();
    String[] reasons = { "元素未找到", "网络超时", "断言失败", "数据异常", "环境不可用" };
    for (String reason : reasons) {
      Map<String, Object> item = new HashMap<>();
      item.put("reason", reason);
      item.put("count", (int) (Math.random() * 50) + 5);
      data.add(item);
    }
    return Result.success(data);
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
}
