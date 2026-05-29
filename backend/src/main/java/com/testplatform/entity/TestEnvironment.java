package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_environment")
public class TestEnvironment {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long projectId;

  private String name;

  private String type;

  private String url;

  private String description;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}
