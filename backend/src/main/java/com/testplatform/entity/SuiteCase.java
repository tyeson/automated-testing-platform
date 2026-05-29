package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("suite_case")
public class SuiteCase {

  private Long suiteId;

  private Long caseId;
}
