package com.testplatform.service;

import com.testplatform.entity.JenkinsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JenkinsService {

  private final IJenkinsConfigService jenkinsConfigService;
  private final RestTemplate restTemplate;

  public Map triggerBuild(Long configId) {
    JenkinsConfig config = jenkinsConfigService.getById(configId);
    if (config == null) {
      throw new RuntimeException("Jenkins配置不存在");
    }

    String url = config.getJenkinsUrl().replaceAll("/+$", "") + "/job/" + config.getJobName() + "/build";
    HttpHeaders headers = createAuthHeaders(config);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

    return Map.of(
        "statusCode", response.getStatusCode().value(),
        "message", response.getStatusCode().is2xxSuccessful() ? "构建已触发" : "触发构建失败"
    );
  }

  @SuppressWarnings("unchecked")
  public Map getBuildStatus(Long configId, Integer buildNumber) {
    JenkinsConfig config = jenkinsConfigService.getById(configId);
    if (config == null) {
      throw new RuntimeException("Jenkins配置不存在");
    }

    String url = config.getJenkinsUrl().replaceAll("/+$", "") + "/job/" + config.getJobName() + "/" + buildNumber + "/api/json";
    HttpHeaders headers = createAuthHeaders(config);

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

    return response.getBody();
  }

  public String getBuildLog(Long configId, Integer buildNumber) {
    JenkinsConfig config = jenkinsConfigService.getById(configId);
    if (config == null) {
      throw new RuntimeException("Jenkins配置不存在");
    }

    String url = config.getJenkinsUrl().replaceAll("/+$", "") + "/job/" + config.getJobName() + "/" + buildNumber + "/consoleText";
    HttpHeaders headers = createAuthHeaders(config);

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

    return response.getBody();
  }

  private HttpHeaders createAuthHeaders(JenkinsConfig config) {
    HttpHeaders headers = new HttpHeaders();
    String auth = config.getUsername() + ":" + config.getApiToken();
    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    headers.set("Authorization", "Basic " + encodedAuth);
    return headers;
  }
}
