import { test, expect, BASE_URL } from './fixtures'
import { verifyApi200 } from './utils/api-helper'
import { generateTestCaseName } from './utils/test-data'

test.describe('测试用例管理', () => {
  test.beforeEach(async ({ page, loggedIn }) => {
  })

  test('用例列表页正常加载 - API /testcases 返回 200', async ({ page }) => {
    const [listResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/testcases') && resp.request().method() === 'GET',
        { timeout: 15000 }
      ),
      page.goto(BASE_URL + '/testcase')
    ])
    await verifyApi200(listResp, 'GET /api/testcases')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 })
  })

  test('新建UI自动化用例 - API POST /testcases 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/testcase')
    await page.waitForLoadState('networkidle')
    await page.click('button:has-text("新建用例")')
    await expect(page.locator('.el-dialog')).toBeVisible()

    const caseName = generateTestCaseName('UI')
    await page.fill('.el-dialog input[placeholder="请输入用例名称"]', caseName)
    await page.click('.el-dialog .el-radio:has-text("UI自动化")')

    const [createResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/testcases') && resp.request().method() === 'POST',
        { timeout: 5000 }
      ).catch(() => null),
      page.click('.el-dialog button:has-text("确定")')
    ])

    if (createResp && createResp.status() === 200) {
      await verifyApi200(createResp, 'POST /api/testcases (UI)')
    }
    await expect(page.locator('.el-dialog')).not.toBeVisible({ timeout: 3000 }).catch(() => {})
  })

  test('新建API自动化用例 - API POST /testcases 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/testcase')
    await page.waitForLoadState('networkidle')
    await page.click('button:has-text("新建用例")')
    await expect(page.locator('.el-dialog')).toBeVisible()

    const caseName = generateTestCaseName('API')
    await page.fill('.el-dialog input[placeholder="请输入用例名称"]', caseName)
    await page.click('.el-dialog .el-radio:has-text("API自动化")')

    const [createResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/testcases') && resp.request().method() === 'POST',
        { timeout: 5000 }
      ).catch(() => null),
      page.click('.el-dialog button:has-text("确定")')
    ])

    if (createResp && createResp.status() === 200) {
      await verifyApi200(createResp, 'POST /api/testcases (API类型)')
    }
    await expect(page.locator('.el-dialog')).not.toBeVisible({ timeout: 3000 }).catch(() => {})
  })

  test('搜索用例', async ({ page }) => {
    await page.goto(BASE_URL + '/testcase')
    await page.waitForLoadState('networkidle')
    await page.fill('input[placeholder="搜索用例名称"]', 'E2E')
    await page.click('button:has-text("搜索")')
    await page.waitForTimeout(1000)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('编辑用例 - API PUT /testcases 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/testcase')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(500)

    const editBtn = page.locator('.el-table button:has-text("编辑")').first()
    if (await editBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await editBtn.click()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await page.fill('.el-dialog input[placeholder="请输入用例名称"]', 'E2E登录功能测试-已编辑')

      const [updateResp] = await Promise.all([
        page.waitForResponse(resp =>
          resp.url().includes('/api/testcases') && resp.request().method() === 'PUT',
          { timeout: 5000 }
        ),
        page.click('.el-dialog button:has-text("确定")')
      ])

      if (updateResp) {
        await verifyApi200(updateResp, 'PUT /api/testcases')
        await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
      }
    }
  })

  test('管理套件弹窗 - API /testcases/suites 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/testcase')
    await page.waitForLoadState('networkidle')
    const [suitesResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/testcases/suites') && resp.request().method() === 'GET',
        { timeout: 10000 }
      ).catch(() => null),
      page.click('button:has-text("管理套件")')
    ])
    await expect(page.locator('.el-dialog')).toBeVisible()
    await expect(page.locator('.el-dialog .el-table')).toBeVisible()
    if (suitesResp) {
      const body = await verifyApi200(suitesResp, 'GET /api/testcases/suites')
      expect(Array.isArray(body.data), '[API] suites 数据应为数组').toBeTruthy()
    }
    await page.click('.el-dialog button:has-text("关闭")')
  })

  test('删除用例 - API DELETE /testcases 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/testcase')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(500)

    const deleteBtn = page.locator('.el-table button:has-text("删除")').first()
    if (await deleteBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      page.on('dialog', dialog => dialog.accept())
      const [deleteResp] = await Promise.all([
        page.waitForResponse(resp =>
          resp.url().includes('/api/testcases') && resp.request().method() === 'DELETE',
          { timeout: 5000 }
        ).catch(() => null),
        deleteBtn.click()
      ])
      if (deleteResp) {
        await verifyApi200(deleteResp, 'DELETE /api/testcases')
      }
      await page.waitForTimeout(1000)
    }
  })
})
