package com.testplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testplatform.common.PageResult;
import com.testplatform.common.Result;
import com.testplatform.entity.Role;
import com.testplatform.entity.User;
import com.testplatform.mapper.RoleMapper;
import com.testplatform.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final IUserService userService;
  private final PasswordEncoder passwordEncoder;
  private final RoleMapper roleMapper;

  @GetMapping
  public Result<PageResult<User>> list(
      @RequestParam(defaultValue = "1") Long current,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) String username) {
    Page<User> page = new Page<>(current, size);
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    if (username != null && !username.isEmpty()) {
      wrapper.like(User::getUsername, username);
    }
    wrapper.orderByDesc(User::getCreateTime);
    Page<User> resultPage = userService.page(page, wrapper);
    for (User user : resultPage.getRecords()) {
      user.setPassword(null);
      List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
      if (!roles.isEmpty()) {
        user.setRoleName(roles.stream().map(Role::getName).collect(Collectors.joining(", ")));
      }
    }
    return Result.success(PageResult.from(resultPage));
  }

  @GetMapping("/{id}")
  public Result<User> getById(@PathVariable Long id) {
    User user = userService.getById(id);
    if (user != null) {
      user.setPassword(null);
    }
    return Result.success(user);
  }

  @PostMapping
  public Result<Void> create(@RequestBody User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setStatus(1);
    userService.save(user);
    return Result.success();
  }

  @PutMapping("/{id}")
  public Result<Void> update(@PathVariable Long id, @RequestBody User user) {
    user.setId(id);
    // Don't update password through this endpoint
    user.setPassword(null);
    userService.updateById(user);
    return Result.success();
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    userService.removeById(id);
    return Result.success();
  }
}
