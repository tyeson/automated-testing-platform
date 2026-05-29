package com.testplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.dto.LoginRequest;
import com.testplatform.dto.LoginResponse;
import com.testplatform.dto.UserInfoResponse;
import com.testplatform.entity.Permission;
import com.testplatform.entity.Role;
import com.testplatform.entity.User;
import com.testplatform.mapper.PermissionMapper;
import com.testplatform.mapper.RoleMapper;
import com.testplatform.mapper.UserMapper;
import com.testplatform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final UserMapper userMapper;
  private final RoleMapper roleMapper;
  private final PermissionMapper permissionMapper;

  public LoginResponse login(LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    String token = jwtUtil.generateToken(request.getUsername());

    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(User::getUsername, request.getUsername());
    User user = userMapper.selectOne(wrapper);

    List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
    List<String> roleNames = roles.stream().map(Role::getCode).collect(Collectors.toList());

    LoginResponse response = new LoginResponse();
    response.setToken(token);
    response.setUsername(user.getUsername());
    response.setEmail(user.getEmail());
    response.setRoles(roleNames);

    return response;
  }

  public UserInfoResponse getCurrentUserInfo(String username) {
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(User::getUsername, username);
    User user = userMapper.selectOne(wrapper);

    if (user == null) {
      throw new RuntimeException("用户不存在");
    }

    List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
    List<Permission> permissions = permissionMapper.selectPermissionsByUserId(user.getId());

    UserInfoResponse response = new UserInfoResponse();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setEmail(user.getEmail());
    response.setStatus(user.getStatus());
    response.setRoles(roles.stream().map(Role::getCode).collect(Collectors.toList()));
    response.setPermissions(permissions.stream().map(Permission::getCode).collect(Collectors.toList()));

    return response;
  }

  public void logout(String token) {
    // In production, you would add the token to a blacklist in Redis
    // For now, we simply clear the security context
    SecurityContextHolder.clearContext();
  }
}
