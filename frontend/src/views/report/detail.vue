<template>
  <div class="report-detail-container">
    <el-page-header @back="$router.back()" title="返回" content="报告详情" style="margin-bottom: 16px" />

    <el-card>
      <template #header>报告概览</template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="项目">{{ report?.projectName }}</el-descriptions-item>
        <el-descriptions-item label="测试套件">{{ report?.suiteName }}</el-descriptions-item>
        <el-descriptions-item label="执行环境">{{ report?.environment }}</el-descriptions-item>
        <el-descriptions-item label="总用例数">{{ report?.totalCases }}</el-descriptions-item>
        <el-descriptions-item label="通过数">{{ report?.passedCases }}</el-descriptions-item>
        <el-descriptions-item label="失败数">{{ report?.failedCases }}</el-descriptions-item>
        <el-descriptions-item label="通过率">
          <el-progress :percentage="report?.passRate || 0" :color="(report?.passRate || 0) >= 90 ? '#67C23A' : '#F56C6C'" />
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ report?.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ report?.endTime }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getReportDetail } from '@/api/modules/report'

const route = useRoute()
const report = ref<any>(null)

async function loadData() {
  const id = Number(route.params.id)
  try {
    const res = await getReportDetail(id)
    report.value = res.data
  } catch {
    report.value = {
      id,
      projectName: '用户服务',
      suiteName: '回归测试',
      environment: 'TEST',
      totalCases: 120,
      passedCases: 115,
      failedCases: 5,
      passRate: 95,
      startTime: '2026-05-29 10:30:00',
      endTime: '2026-05-29 10:45:30'
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.report-detail-container {
  // styles
}
</style>
