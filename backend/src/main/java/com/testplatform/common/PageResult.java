package com.testplatform.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

  private long current;
  private long size;
  private long total;
  private java.util.List<T> records;

  public static <T> PageResult<T> from(IPage<T> page) {
    return new PageResult<>(
        page.getCurrent(),
        page.getSize(),
        page.getTotal(),
        page.getRecords());
  }

  public static <T> PageResult<T> empty(long current, long size) {
    return new PageResult<>(current, size, 0, java.util.Collections.emptyList());
  }
}
