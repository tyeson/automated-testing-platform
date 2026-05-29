package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role")
public class Role {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String name;

  private String code;

  private String description;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;
}
