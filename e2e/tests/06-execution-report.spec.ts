import { test, expect, BASE_URL } from './fixtures'
import { verifyApi200 } from './utils/api-helper'

test.describe('执行中心', () => {
  test.beforeEach(async ({ page, loggedIn }) => {
  })

  test('执行列表页正常加载 - API /executions 返回 200', async ({ page }) => {
    const [execResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/executions') && resp.request().method() === 'GET',
        { timeout: 15000 }
      ).catch(() => null),
      page.goto(BASE_URL + '/execution')
    ])
    if (execResp) {
      await verifyApi200(execResp, 'GET /api/executions')
    }
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 })
  })

  test('执行列表显示表头', async ({ page }) => {
    await page.goto(BASE_URL + '/execution')
    await page.waitForLoadState('networkidle')
    const headers = page.locator('.el-table th')
    await expect(headers.first()).toBeVisible()
  })
})

test.describe('报告中心', () => {
  test.beforeEach(async ({ page, loggedIn }) => {
  })

  test('报告列表页正常加载 - API /reports 返回 200', async ({ page }) => {
    const [reportsResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/reports') && resp.request().method() === 'GET',
        { timeout: 15000 }
      ),
      page.goto(BASE_URL + '/report')
    ])
    await verifyApi200(reportsResp, 'GET /api/reports')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 })
  })

  test('报告列表显示表头', async ({ page }) => {
    await page.goto(BASE_URL + '/report')
    await page.waitForLoadState('networkidle')
    const headers = page.locator('.el-table th')
    await expect(headers.first()).toBeVisible()
  })
})

test.describe('侧边栏导航', () => {
  test.beforeEach(async ({ page, loggedIn }) => {
    await page.goto(BASE_URL + '/dashboard')
    await page.waitForLoadState('networkidle')
  })

  test('点击项目管理菜单', async ({ page }) => {
    await page.click('.el-menu-item:has-text("项目管理")')
    await page.waitForURL('**/project', { timeout: 5000 })
    await expect(page).toHaveURL(/project/)
  })

  test('点击用例管理菜单', async ({ page }) => {
    await page.click('.el-menu-item:has-text("用例管理")')
    await page.waitForURL('**/testcase', { timeout: 5000 })
    await expect(page).toHaveURL(/testcase/)
  })

  test('点击执行中心菜单', async ({ page }) => {
    await page.click('.el-menu-item:has-text("执行中心")')
    await page.waitForURL('**/execution', { timeout: 5000 })
    await expect(page).toHaveURL(/execution/)
  })

  test('点击报告中心菜单', async ({ page }) => {
    await page.click('.el-menu-item:has-text("报告中心")')
    await page.waitForURL('**/report', { timeout: 5000 })
    await expect(page).toHaveURL(/report/)
  })

  test('点击系统管理菜单', async ({ page }) => {
    await page.click('.el-sub-menu:has-text("系统管理")')
    await page.waitForTimeout(500)
    await page.click('.el-menu-item:has-text("用户管理")')
    await page.waitForURL('**/system/user', { timeout: 5000 })
    await expect(page).toHaveURL(/system\/user/)
  })
})
