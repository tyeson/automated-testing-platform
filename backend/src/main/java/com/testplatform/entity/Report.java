package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long executionId;

  private String title;

  private String summary;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;
}
