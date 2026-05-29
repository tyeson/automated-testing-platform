package com.testplatform.controller;

import com.testplatform.common.Result;
import com.testplatform.dto.LoginRequest;
import com.testplatform.dto.LoginResponse;
import com.testplatform.dto.UserInfoResponse;
import com.testplatform.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return Result.success(response);
  }

  @PostMapping("/logout")
  public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
    if (token != null && token.startsWith("Bearer ")) {
      authService.logout(token.substring(7));
    }
    return Result.success();
  }

  @GetMapping("/info")
  public Result<UserInfoResponse> getCurrentUser(Authentication authentication) {
    String username = authentication.getName();
    UserInfoResponse info = authService.getCurrentUserInfo(username);
    return Result.success(info);
  }
}
