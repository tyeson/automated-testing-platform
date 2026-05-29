package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("permission")
public class Permission {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String name;

  private String code;

  private String type;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;
}
