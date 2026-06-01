package com.testplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {

  long countAllExecutions();

  long countByStatus(@Param("status") String status);

  long countProjects();

  List<Map<String, Object>> trendByDay(@Param("days") int days);

  List<Map<String, Object>> fillMissingTrendDays(@Param("days") int days);

  List<Map<String, Object>> environmentStats();

  List<Map<String, Object>> failureReasons();

  List<Map<String, Object>> projectExecutionRanking();

  List<Map<String, Object>> statusDistribution();
}
