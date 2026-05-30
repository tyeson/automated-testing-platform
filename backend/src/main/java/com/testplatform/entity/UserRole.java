package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("user_role")
public class UserRole {

  @TableId(type = IdType.NONE)
  private Long userId;

  private Long roleId;
}
