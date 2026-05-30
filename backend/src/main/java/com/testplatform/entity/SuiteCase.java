package com.testplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("suite_case")
public class SuiteCase {

  @TableId(type = IdType.NONE)
  private Long suiteId;

  private Long caseId;
}
