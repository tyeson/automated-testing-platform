package com.testplatform.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.entity.Permission;
import com.testplatform.entity.Role;
import com.testplatform.entity.User;
import com.testplatform.mapper.PermissionMapper;
import com.testplatform.mapper.RoleMapper;
import com.testplatform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserMapper userMapper;
  private final RoleMapper roleMapper;
  private final PermissionMapper permissionMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(User::getUsername, username);
    User user = userMapper.selectOne(wrapper);

    if (user == null) {
      throw new UsernameNotFoundException("用户不存在: " + username);
    }

    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

    // Load roles
    List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
    for (Role role : roles) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
    }

    // Load permissions
    List<Permission> permissions = permissionMapper.selectPermissionsByUserId(user.getId());
    for (Permission permission : permissions) {
      authorities.add(new SimpleGrantedAuthority(permission.getCode()));
    }

    return new org.springframework.security.core.userdetails.User(
        user.getUsername(),
        user.getPassword(),
        user.getStatus() == 1,
        true, true, true,
        authorities);
  }
}
