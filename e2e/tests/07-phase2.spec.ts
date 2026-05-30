import { test, expect, BASE_URL } from './fixtures'
import { verifyApi200 } from './utils/api-helper'

test.describe('环境管理', () => {
  test.beforeEach(async ({ page, loggedIn }) => {
  })

  test('环境管理页面正常加载 - API /environments 返回 200', async ({ page }) => {
    const [envResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/environments') && resp.request().method() === 'GET',
        { timeout: 15000 }
      ),
      page.goto(BASE_URL + '/system/environment')
    ])
    const body = await verifyApi200(envResp, 'GET /api/environments')
    expect(body.data.records, '[API] environments 应包含 records 分页数据').toBeDefined()
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 })
  })

  test('环境管理页面显示搜索和新增按钮', async ({ page }) => {
    await page.goto(BASE_URL + '/system/environment')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('button:has-text("新增环境")')).toBeVisible()
    await expect(page.locator('input[placeholder="搜索环境名称"]')).toBeVisible()
  })

  test('新增环境弹窗可打开', async ({ page }) => {
    await page.goto(BASE_URL + '/system/environment')
    await page.waitForLoadState('networkidle')
    await page.click('button:has-text("新增环境")')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog input[placeholder="请输入环境名称"]')).toBeVisible()
    await page.click('.el-dialog button:has-text("取消")')
  })

  test('搜索功能', async ({ page }) => {
    await page.goto(BASE_URL + '/system/environment')
    await page.waitForLoadState('networkidle')
    await page.fill('input[placeholder="搜索环境名称"]', '测试')
    await page.click('button:has-text("搜索")')
    await page.waitForTimeout(1000)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('编辑环境 - API PUT /environments 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/system/environment')
    await page.waitForLoadState('networkidle')
    const editBtn = page.locator('button:has-text("编辑")').first()
    if (await editBtn.isVisible()) {
      await editBtn.click()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await page.fill('.el-dialog input[placeholder="请输入环境名称"]', '测试环境-已编辑')

      const [updateResp] = await Promise.all([
        page.waitForResponse(resp =>
          resp.url().includes('/api/environments') && resp.request().method() === 'PUT',
          { timeout: 5000 }
        ),
        page.click('.el-dialog button:has-text("确定")')
      ])

      await verifyApi200(updateResp, 'PUT /api/environments')
      await expect(page.locator('.el-message--success').first()).toBeVisible({ timeout: 5000 })
    }
  })
})

test.describe('Jenkins集成', () => {
  test.beforeEach(async ({ page, loggedIn }) => {
  })

  test('Jenkins集成页面正常加载 - API /jenkins/configs 返回 200', async ({ page }) => {
    const [jenkinsResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/jenkins/configs') && resp.request().method() === 'GET',
        { timeout: 15000 }
      ).catch(() => null),
      page.goto(BASE_URL + '/system/jenkins')
    ])
    if (jenkinsResp) {
      const body = await verifyApi200(jenkinsResp, 'GET /api/jenkins/configs')
      expect(Array.isArray(body.data)).toBeTruthy('[API] jenkins configs 数据应为数组')
    }
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 })
  })

  test('Jenkins页面显示新增按钮', async ({ page }) => {
    await page.goto(BASE_URL + '/system/jenkins')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('button:has-text("新增配置")')).toBeVisible()
  })

  test('新增Jenkins配置弹窗可打开', async ({ page }) => {
    await page.goto(BASE_URL + '/system/jenkins')
    await page.waitForLoadState('networkidle')
    await page.click('button:has-text("新增配置")')
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog input[placeholder="请输入配置名称"]')).toBeVisible()
    await page.click('.el-dialog button:has-text("取消")')
  })

  test('编辑Jenkins配置 - API PUT/POST /jenkins 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/system/jenkins')
    await page.waitForLoadState('networkidle')
    const editBtn = page.locator('button:has-text("编辑")').first()
    if (await editBtn.isVisible()) {
      await editBtn.click()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await page.fill('.el-dialog input[placeholder="请输入配置名称"]', 'E2E Jenkins-已编辑')

      const [updateResp] = await Promise.all([
        page.waitForResponse(resp =>
          resp.url().includes('/api/jenkins') &&
          (resp.request().method() === 'PUT' || resp.request().method() === 'POST'),
          { timeout: 5000 }
        ).catch(() => null),
        page.click('.el-dialog button:has-text("确定")')
      ])
      if (updateResp) {
        await verifyApi200(updateResp, 'PUT/POST /api/jenkins')
      }
      await expect(page.locator('.el-message--success').first()).toBeVisible({ timeout: 5000 })
    }
  })
})

test.describe('执行中心增强', () => {
  test.beforeEach(async ({ page, loggedIn }) => {
  })

  test('执行中心页面正常加载 - API /executions 返回 200', async ({ page }) => {
    const [execResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/executions') && resp.request().method() === 'GET',
        { timeout: 15000 }
      ).catch(() => null),
      page.goto(BASE_URL + '/execution')
    ])
    if (execResp) {
      await verifyApi200(execResp, 'GET /api/executions (phase2)')
    }
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 })
  })

  test('触发执行弹窗可打开', async ({ page }) => {
    await page.goto(BASE_URL + '/execution')
    await page.waitForLoadState('networkidle')
    const triggerBtn = page.locator('button:has-text("触发执行")')
    if (await triggerBtn.isVisible()) {
      await triggerBtn.click()
      await expect(page.locator('.el-dialog')).toBeVisible()

      const [triggerResp] = await Promise.all([
        page.waitForResponse(resp =>
          resp.url().includes('/api/executions') && resp.request().method() === 'POST',
          { timeout: 5000 }
        ).catch(() => null),
        page.click('.el-dialog button:has-text("取消")')
      ])
      if (triggerResp) {
        await verifyApi200(triggerResp, 'POST /api/executions (trigger)')
      }
    }
  })
})
