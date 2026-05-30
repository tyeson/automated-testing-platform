package com.testplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testplatform.entity.TestSuite;
import com.testplatform.mapper.TestSuiteMapper;
import com.testplatform.service.ITestSuiteService;
import org.springframework.stereotype.Service;

@Service
public class TestSuiteServiceImpl extends ServiceImpl<TestSuiteMapper, TestSuite> implements ITestSuiteService {
}
