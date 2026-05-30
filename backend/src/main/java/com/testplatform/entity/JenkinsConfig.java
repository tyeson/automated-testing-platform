package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("jenkins_config")
public class JenkinsConfig {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long projectId;

  private String name;

  private String jenkinsUrl;

  private String username;

  private String apiToken;

  private String jobName;

  private Integer status;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}
