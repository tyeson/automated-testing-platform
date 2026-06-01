package com.testplatform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.testplatform.entity.ExecutionLog;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
public class ExecutionLogVO {

  private Long id;
  private Long executionId;
  private Long caseId;
  private String caseName;
  private String status;
  private String errorMessage;
  private String screenshotPath;
  private String videoPath;
  private String consoleLog;
  private Long duration;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime startTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime endTime;

  public static ExecutionLogVO from(ExecutionLog log, String caseName) {
    ExecutionLogVO vo = new ExecutionLogVO();
    vo.setId(log.getId());
    vo.setExecutionId(log.getExecutionId());
    vo.setCaseId(log.getCaseId());
    vo.setCaseName(caseName);
    vo.setStatus(log.getStatus());
    vo.setErrorMessage(log.getErrorMessage());
    vo.setScreenshotPath(log.getScreenshotPath());
    vo.setVideoPath(log.getVideoPath());
    vo.setConsoleLog(log.getConsoleLog());
    vo.setStartTime(log.getStartTime());
    vo.setEndTime(log.getEndTime());
    if (log.getStartTime() != null && log.getEndTime() != null) {
      vo.setDuration(ChronoUnit.SECONDS.between(log.getStartTime(), log.getEndTime()));
    } else {
      vo.setDuration(0L);
    }
    return vo;
  }
}
