package com.testplatform.dto;

import lombok.Data;

@Data
public class LoginResponse {

  private String token;
  private String username;
  private String email;
  private java.util.List<String> roles;
}
