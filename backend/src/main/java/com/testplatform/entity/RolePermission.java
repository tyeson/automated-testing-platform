package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("role_permission")
public class RolePermission {

  @TableId(type = IdType.NONE)
  private Long roleId;

  private Long permissionId;
}
