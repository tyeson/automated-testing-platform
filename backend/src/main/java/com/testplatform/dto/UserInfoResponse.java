package com.testplatform.dto;

import lombok.Data;

@Data
public class UserInfoResponse {
  private Long id;
  private String username;
  private String email;
  private Integer status;
  private java.util.List<String> roles;
  private java.util.List<String> permissions;
}
