package com.testplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.entity.TestEnvironment;
import com.testplatform.service.ITestEnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/environments")
@RequiredArgsConstructor
public class EnvironmentController {

  private final ITestEnvironmentService testEnvironmentService;

  @GetMapping
  public Result<PageResult<TestEnvironment>> list(
      @RequestParam(required = false) Long projectId,
      @RequestParam(defaultValue = "1") Long current,
      @RequestParam(defaultValue = "10") Long size) {
    Page<TestEnvironment> page = new Page<>(current, size);
    LambdaQueryWrapper<TestEnvironment> wrapper = new LambdaQueryWrapper<>();
    if (projectId != null) {
      wrapper.eq(TestEnvironment::getProjectId, projectId);
    }
    wrapper.orderByDesc(TestEnvironment::getCreateTime);
    return Result.success(PageResult.from(testEnvironmentService.page(page, wrapper)));
  }

  @GetMapping("/{id}")
  public Result<TestEnvironment> getById(@PathVariable Long id) {
    return Result.success(testEnvironmentService.getById(id));
  }

  @PostMapping
  public Result<Void> create(@RequestBody TestEnvironment testEnvironment) {
    testEnvironmentService.save(testEnvironment);
    return Result.success();
  }

  @PutMapping("/{id}")
  public Result<Void> update(@PathVariable Long id, @RequestBody TestEnvironment testEnvironment) {
    testEnvironment.setId(id);
    testEnvironmentService.updateById(testEnvironment);
    return Result.success();
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    testEnvironmentService.removeById(id);
    return Result.success();
  }
}
