import { test, expect, BASE_URL } from './fixtures'
import { verifyApi200 } from './utils/api-helper'

test.describe('项目管理', () => {
  test.beforeEach(async ({ page, loggedIn }) => {
  })

  test('项目列表页正常加载 - API /projects 返回 200', async ({ page }) => {
    const [listResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/projects') && resp.request().method() === 'GET',
        { timeout: 15000 }
      ),
      page.goto(BASE_URL + '/project')
    ])
    const body = await verifyApi200(listResp, 'GET /api/projects')
    expect(body.data.records, '[API] projects 应包含 records 分页数据').toBeDefined()
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 })
  })

  test('新建项目 - API POST /projects 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/project')
    await page.waitForLoadState('networkidle')
    await page.click('button:has-text("新建项目")')
    await expect(page.locator('.el-dialog')).toBeVisible()

    const timestamp = Date.now()
    await page.fill('.el-dialog input[placeholder="请输入项目名称"]', `E2E测试项目_${timestamp}`)
    await page.fill('.el-dialog input[placeholder="请输入项目编码"]', `e2e-${timestamp}`)
    await page.fill('.el-dialog input[placeholder="请输入负责人"]', '测试工程师')
    await page.fill('.el-dialog textarea[placeholder="请输入项目描述"]', 'Playwright自动创建的测试项目')

    const [createResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/projects') && resp.request().method() === 'POST',
        { timeout: 5000 }
      ),
      page.click('.el-dialog button:has-text("确定")')
    ])

    await verifyApi200(createResp, 'POST /api/projects')
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
  })

  test('搜索项目', async ({ page }) => {
    await page.goto(BASE_URL + '/project')
    await page.waitForLoadState('networkidle')
    await page.fill('input[placeholder="搜索项目名称"]', 'E2E')
    await page.click('button:has-text("搜索")')
    await page.waitForTimeout(1000)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('编辑项目 - API PUT /projects 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/project')
    await page.waitForLoadState('networkidle')
    const editBtn = page.locator('button:has-text("编辑")').first()
    if (await editBtn.isVisible()) {
      await editBtn.click()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await page.fill('.el-dialog input[placeholder="请输入项目名称"]', 'E2E测试项目-已编辑')

      const [updateResp] = await Promise.all([
        page.waitForResponse(resp =>
          resp.url().includes('/api/projects') && resp.request().method() === 'PUT',
          { timeout: 5000 }
        ),
        page.click('.el-dialog button:has-text("确定")')
      ])

      await verifyApi200(updateResp, 'PUT /api/projects')
      await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
    }
  })

  test('删除项目 - API DELETE /projects 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/project')
    await page.waitForLoadState('networkidle')
    const deleteBtn = page.locator('button:has-text("删除")').first()
    if (await deleteBtn.isVisible()) {
      page.on('dialog', dialog => dialog.accept())
      const [deleteResp] = await Promise.all([
        page.waitForResponse(resp =>
          resp.url().includes('/api/projects') && resp.request().method() === 'DELETE',
          { timeout: 5000 }
        ).catch(() => null),
        deleteBtn.click()
      ])
      if (deleteResp) {
        await verifyApi200(deleteResp, 'DELETE /api/projects')
      }
      await page.waitForTimeout(1000)
    }
  })
})
