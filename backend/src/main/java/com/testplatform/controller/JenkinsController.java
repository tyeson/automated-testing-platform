package com.testplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.common.Result;
import com.testplatform.entity.JenkinsConfig;
import com.testplatform.service.IJenkinsConfigService;
import com.testplatform.service.JenkinsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jenkins")
@RequiredArgsConstructor
public class JenkinsController {

  private final IJenkinsConfigService jenkinsConfigService;
  private final JenkinsService jenkinsService;

  @GetMapping("/configs")
  public Result<List<JenkinsConfig>> listConfigs(@RequestParam(required = false) Long projectId) {
    LambdaQueryWrapper<JenkinsConfig> wrapper = new LambdaQueryWrapper<>();
    if (projectId != null) {
      wrapper.eq(JenkinsConfig::getProjectId, projectId);
    }
    wrapper.orderByDesc(JenkinsConfig::getCreateTime);
    return Result.success(jenkinsConfigService.list(wrapper));
  }

  @PostMapping("/configs")
  public Result<Void> createConfig(@RequestBody JenkinsConfig jenkinsConfig) {
    jenkinsConfigService.save(jenkinsConfig);
    return Result.success();
  }

  @PutMapping("/configs/{id}")
  public Result<Void> updateConfig(@PathVariable Long id, @RequestBody JenkinsConfig jenkinsConfig) {
    jenkinsConfig.setId(id);
    jenkinsConfigService.updateById(jenkinsConfig);
    return Result.success();
  }

  @DeleteMapping("/configs/{id}")
  public Result<Void> deleteConfig(@PathVariable Long id) {
    jenkinsConfigService.removeById(id);
    return Result.success();
  }

  @PostMapping("/trigger/{configId}")
  public Result<Map> triggerBuild(@PathVariable Long configId) {
    return Result.success(jenkinsService.triggerBuild(configId));
  }

  @GetMapping("/status/{configId}/{buildNumber}")
  public Result<Map> getBuildStatus(@PathVariable Long configId, @PathVariable Integer buildNumber) {
    return Result.success(jenkinsService.getBuildStatus(configId, buildNumber));
  }

  @GetMapping("/log/{configId}/{buildNumber}")
  public Result<String> getBuildLog(@PathVariable Long configId, @PathVariable Integer buildNumber) {
    return Result.success(jenkinsService.getBuildLog(configId, buildNumber));
  }
}
