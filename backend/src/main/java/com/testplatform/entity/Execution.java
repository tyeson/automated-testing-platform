package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("execution")
public class Execution {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long projectId;

  private Long suiteId;

  private Long caseId;

  private String triggerType;

  private String status;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private Integer totalCount;

  private Integer passCount;

  private Integer failCount;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;
}
