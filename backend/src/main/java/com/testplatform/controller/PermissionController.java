package com.testplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.Result;
import com.testplatform.entity.Permission;
import com.testplatform.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

  private final IPermissionService permissionService;

  @GetMapping
  public Result<List<Permission>> list() {
    LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
    wrapper.orderByAsc(Permission::getId);
    return Result.success(permissionService.list(wrapper));
  }

  @GetMapping("/{id}")
  public Result<Permission> getById(@PathVariable Long id) {
    return Result.success(permissionService.getById(id));
  }

  @PostMapping
  public Result<Void> create(@RequestBody Permission permission) {
    permissionService.save(permission);
    return Result.success();
  }

  @PutMapping("/{id}")
  public Result<Void> update(@PathVariable Long id, @RequestBody Permission permission) {
    permission.setId(id);
    permissionService.updateById(permission);
    return Result.success();
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    permissionService.removeById(id);
    return Result.success();
  }
}
