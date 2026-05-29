package com.testplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.entity.Project;
import com.testplatform.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

  private final IProjectService projectService;

  @GetMapping
  public Result<PageResult<Project>> list(
      @RequestParam(defaultValue = "1") Long current,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) String name) {
    Page<Project> page = new Page<>(current, size);
    LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
    if (name != null && !name.isEmpty()) {
      wrapper.like(Project::getName, name);
    }
    wrapper.orderByDesc(Project::getCreateTime);
    return Result.success(PageResult.from(projectService.page(page, wrapper)));
  }

  @GetMapping("/{id}")
  public Result<Project> getById(@PathVariable Long id) {
    return Result.success(projectService.getById(id));
  }

  @PostMapping
  public Result<Void> create(@RequestBody Project project) {
    projectService.save(project);
    return Result.success();
  }

  @PutMapping("/{id}")
  public Result<Void> update(@PathVariable Long id, @RequestBody Project project) {
    project.setId(id);
    projectService.updateById(project);
    return Result.success();
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    projectService.removeById(id);
    return Result.success();
  }
}
