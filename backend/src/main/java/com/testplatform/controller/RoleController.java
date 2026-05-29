package com.testplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.entity.Permission;
import com.testplatform.entity.Role;
import com.testplatform.entity.RolePermission;
import com.testplatform.mapper.PermissionMapper;
import com.testplatform.mapper.RolePermissionMapper;
import com.testplatform.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

  private final IRoleService roleService;
  private final RolePermissionMapper rolePermissionMapper;
  private final PermissionMapper permissionMapper;

  @GetMapping
  public Result<PageResult<Role>> list(
      @RequestParam(defaultValue = "1") Long current,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) String name) {
    Page<Role> page = new Page<>(current, size);
    LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
    if (name != null && !name.isEmpty()) {
      wrapper.like(Role::getName, name);
    }
    wrapper.orderByDesc(Role::getCreateTime);
    return Result.success(PageResult.from(roleService.page(page, wrapper)));
  }

  @GetMapping("/{id}")
  public Result<Role> getById(@PathVariable Long id) {
    return Result.success(roleService.getById(id));
  }

  @PostMapping
  public Result<Void> create(@RequestBody Role role) {
    roleService.save(role);
    return Result.success();
  }

  @PutMapping("/{id}")
  public Result<Void> update(@PathVariable Long id, @RequestBody Role role) {
    role.setId(id);
    roleService.updateById(role);
    return Result.success();
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    roleService.removeById(id);
    return Result.success();
  }

  @GetMapping("/{id}/permissions")
  public Result<List<Permission>> getRolePermissions(@PathVariable Long id) {
    List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
        new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));
    List<Long> permissionIds = rolePermissions.stream()
        .map(RolePermission::getPermissionId)
        .toList();
    if (permissionIds.isEmpty()) {
      return Result.success(List.of());
    }
    return Result.success(permissionMapper.selectBatchIds(permissionIds));
  }

  @PutMapping("/{id}/permissions")
  public Result<Void> assignRolePermissions(
      @PathVariable Long id,
      @RequestBody AssignPermissionsRequest request) {
    rolePermissionMapper.delete(
        new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));
    if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
      for (Long permissionId : request.getPermissionIds()) {
        RolePermission rp = new RolePermission();
        rp.setRoleId(id);
        rp.setPermissionId(permissionId);
        rolePermissionMapper.insert(rp);
      }
    }
    return Result.success();
  }

  @lombok.Data
  public static class AssignPermissionsRequest {
    private List<Long> permissionIds;
  }
}
