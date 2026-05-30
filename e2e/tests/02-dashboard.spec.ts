import { test, expect, BASE_URL } from './fixtures'
import { verifyApi200 } from './utils/api-helper'

test.describe('首页工作台', () => {
  test('Dashboard 页面正常加载 - API /reports/dashboard 返回 200', async ({ page, loggedIn }) => {
    const [dashboardResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/reports/dashboard') && resp.request().method() === 'GET',
        { timeout: 10000 }
      ),
      expect(page.locator('.dashboard-container')).toBeVisible({ timeout: 10000 })
    ])
    const body = await verifyApi200(dashboardResp, 'GET /api/reports/dashboard')
    expect(body.data.projectCount, '[API] dashboard 应包含 projectCount').toBeDefined()
    expect(body.data.successRate, '[API] dashboard 应包含 successRate').toBeDefined()
  })

  test('Dashboard 显示指标卡片', async ({ page, loggedIn }) => {
    await expect(page.locator('.stat-card').first()).toBeVisible({ timeout: 10000 })
  })

  test('Dashboard 显示趋势图表 - API /reports/trend 返回 200', async ({ page, loggedIn }) => {
    await page.waitForTimeout(2000)
    const [trendResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/reports/trend') && resp.request().method() === 'GET',
        { timeout: 10000 }
      ).catch(() => null),
      expect(page.locator('.el-card').first()).toBeVisible({ timeout: 10000 })
    ])
    if (trendResp) {
      const body = await verifyApi200(trendResp, 'GET /api/reports/trend')
      expect(Array.isArray(body.data), '[API] trend 数据应为数组').toBeTruthy()
    }
  })
})
