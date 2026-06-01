package com.testplatform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.testplatform.entity.Execution;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Data
public class ExecutionDetailVO {

  private Long id;
  private Long projectId;
  private String projectName;
  private String projectCode;
  private Long suiteId;
  private String suiteName;
  private Long caseId;
  private String caseName;
  private String triggerType;
  private String status;
  private Integer totalCases;
  private Integer passedCases;
  private Integer failedCases;
  private Integer skippedCases;
  private Integer passRate;
  private Long duration;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime startTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime endTime;

  private String environment;
  private String creator;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;

  private List<ExecutionLogVO> logs;

  public static ExecutionDetailVO from(Execution e) {
    return from(e, null, null, null, null, null);
  }

  public static ExecutionDetailVO from(
      Execution e,
      String projectName,
      String projectCode,
      String suiteName,
      String caseName,
      List<ExecutionLogVO> logs) {
    ExecutionDetailVO vo = new ExecutionDetailVO();
    vo.setId(e.getId());
    vo.setProjectId(e.getProjectId());
    vo.setProjectName(projectName);
    vo.setProjectCode(projectCode);
    vo.setSuiteId(e.getSuiteId());
    vo.setSuiteName(suiteName);
    vo.setCaseId(e.getCaseId());
    vo.setCaseName(caseName);
    vo.setTriggerType(e.getTriggerType());
    vo.setStatus(e.getStatus());
    vo.setTotalCases(e.getTotalCount() == null ? 0 : e.getTotalCount());
    vo.setPassedCases(e.getPassCount() == null ? 0 : e.getPassCount());
    vo.setFailedCases(e.getFailCount() == null ? 0 : e.getFailCount());
    vo.setSkippedCases(0);
    int total = vo.getTotalCases();
    vo.setPassRate(total > 0 ? (int) Math.round(vo.getPassedCases() * 100.0 / total) : 0);
    if (e.getStartTime() != null && e.getEndTime() != null) {
      vo.setDuration(ChronoUnit.SECONDS.between(e.getStartTime(), e.getEndTime()));
    } else if (e.getStartTime() != null) {
      vo.setDuration(ChronoUnit.SECONDS.between(e.getStartTime(), LocalDateTime.now()));
    } else {
      vo.setDuration(0L);
    }
    vo.setStartTime(e.getStartTime());
    vo.setEndTime(e.getEndTime());
    vo.setCreateTime(e.getCreateTime());
    vo.setLogs(logs);
    return vo;
  }
}
