package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_case")
public class TestCase {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long projectId;

  private String name;

  private String type;

  private String priority;

  private String tags;

  private String gitPath;

  private String description;

  private String creator;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}
