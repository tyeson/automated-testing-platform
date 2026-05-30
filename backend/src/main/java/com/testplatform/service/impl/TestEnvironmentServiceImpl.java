package com.testplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testplatform.entity.TestEnvironment;
import com.testplatform.mapper.TestEnvironmentMapper;
import com.testplatform.service.ITestEnvironmentService;
import org.springframework.stereotype.Service;

@Service
public class TestEnvironmentServiceImpl extends ServiceImpl<TestEnvironmentMapper, TestEnvironment> implements ITestEnvironmentService {
}
