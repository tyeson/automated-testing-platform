package com.testplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.entity.TestCase;
import com.testplatform.entity.TestSuite;
import com.testplatform.service.ITestCaseService;
import com.testplatform.service.ITestSuiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testcases")
@RequiredArgsConstructor
public class TestCaseController {

  private final ITestCaseService testCaseService;
  private final ITestSuiteService testSuiteService;

  @GetMapping
  public Result<PageResult<TestCase>> list(
      @RequestParam(defaultValue = "1") Long current,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) Long projectId,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String name) {
    Page<TestCase> page = new Page<>(current, size);
    LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
    if (projectId != null) {
      wrapper.eq(TestCase::getProjectId, projectId);
    }
    if (type != null && !type.isEmpty()) {
      wrapper.eq(TestCase::getType, type);
    }
    if (name != null && !name.isEmpty()) {
      wrapper.like(TestCase::getName, name);
    }
    wrapper.orderByDesc(TestCase::getCreateTime);
    return Result.success(PageResult.from(testCaseService.page(page, wrapper)));
  }

  @GetMapping("/{id}")
  public Result<TestCase> getById(@PathVariable Long id) {
    return Result.success(testCaseService.getById(id));
  }

  @PostMapping
  public Result<Void> create(@RequestBody TestCase testCase) {
    testCaseService.save(testCase);
    return Result.success();
  }

  @PutMapping("/{id}")
  public Result<Void> update(@PathVariable Long id, @RequestBody TestCase testCase) {
    testCase.setId(id);
    testCaseService.updateById(testCase);
    return Result.success();
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    testCaseService.removeById(id);
    return Result.success();
  }

  @GetMapping("/suites")
  public Result<List<TestSuite>> listSuites(@RequestParam Long projectId) {
    LambdaQueryWrapper<TestSuite> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(TestSuite::getProjectId, projectId);
    wrapper.orderByDesc(TestSuite::getCreateTime);
    return Result.success(testSuiteService.list(wrapper));
  }

  @PostMapping("/suites")
  public Result<Void> createSuite(@RequestBody TestSuite suite) {
    testSuiteService.save(suite);
    return Result.success();
  }

  @PutMapping("/suites/{id}")
  public Result<Void> updateSuite(@PathVariable Long id, @RequestBody TestSuite suite) {
    suite.setId(id);
    testSuiteService.updateById(suite);
    return Result.success();
  }

  @DeleteMapping("/suites/{id}")
  public Result<Void> deleteSuite(@PathVariable Long id) {
    testSuiteService.removeById(id);
    return Result.success();
  }
}
