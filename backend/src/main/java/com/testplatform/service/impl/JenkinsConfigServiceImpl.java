package com.testplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testplatform.entity.JenkinsConfig;
import com.testplatform.mapper.JenkinsConfigMapper;
import com.testplatform.service.IJenkinsConfigService;
import org.springframework.stereotype.Service;

@Service
public class JenkinsConfigServiceImpl extends ServiceImpl<JenkinsConfigMapper, JenkinsConfig> implements IJenkinsConfigService {
}
