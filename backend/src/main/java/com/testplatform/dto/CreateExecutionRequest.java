package com.testplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateExecutionRequest {

  @NotNull(message = "项目ID不能为空")
  private Long projectId;

  private Long suiteId;

  private Long caseId;

  @NotBlank(message = "触发类型不能为空")
  private String triggerType;
}
