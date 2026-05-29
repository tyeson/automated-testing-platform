package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("execution_log")
public class ExecutionLog {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long executionId;

  private Long caseId;

  private String status;

  private String errorMessage;

  private String screenshotPath;

  private String videoPath;

  private String consoleLog;

  private LocalDateTime startTime;

  private LocalDateTime endTime;
}
