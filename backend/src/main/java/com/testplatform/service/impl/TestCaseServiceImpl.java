package com.testplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testplatform.entity.TestCase;
import com.testplatform.mapper.TestCaseMapper;
import com.testplatform.service.ITestCaseService;
import org.springframework.stereotype.Service;

@Service
public class TestCaseServiceImpl extends ServiceImpl<TestCaseMapper, TestCase> implements ITestCaseService {
}
