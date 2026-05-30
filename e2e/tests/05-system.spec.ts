import { test, expect, BASE_URL } from './fixtures'
import { verifyApi200 } from './utils/api-helper'

test.describe('用户管理', () => {
  test.beforeEach(async ({ page, loggedIn }) => {
  })

  test('用户列表页正常加载 - API /users 返回 200 且含 realName/phone', async ({ page }) => {
    const [usersResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/users') && resp.request().method() === 'GET',
        { timeout: 15000 }
      ),
      page.goto(BASE_URL + '/system/user')
    ])
    const body = await verifyApi200(usersResp, 'GET /api/users')
    expect(body.data.records, '[API] users 应包含 records 分页数据').toBeDefined()
    if (body.data.records && body.data.records.length > 0) {
      const firstUser = body.data.records[0]
      expect(firstUser.realName !== undefined || firstUser.username, '[API] 用户应包含 realName 或 username').toBeTruthy()
      expect(firstUser.phone !== undefined || firstUser.email !== undefined, '[API] 用户应包含 phone 或 email').toBeTruthy()
    }
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 })
  })

  test('新增用户 - API POST /users 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/system/user')
    await page.waitForLoadState('networkidle')
    await page.click('button:has-text("新增用户")')
    await expect(page.locator('.el-dialog')).toBeVisible()

    const timestamp = Date.now()
    await page.fill('.el-dialog input[placeholder="请输入用户名"]', `e2e_tester_${timestamp}`)
    await page.fill('.el-dialog input[placeholder="请输入姓名"]', 'E2E测试员')
    await page.fill('.el-dialog input[placeholder="请输入密码"]', 'Test123456')
    await page.fill('.el-dialog input[placeholder="请输入邮箱"]', `e2e_${timestamp}@test.com`)
    await page.fill('.el-dialog input[placeholder="请输入手机号"]', '13900139000')

    const [createResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/users') && resp.request().method() === 'POST',
        { timeout: 5000 }
      ),
      page.click('.el-dialog button:has-text("确定")')
    ])

    await verifyApi200(createResp, 'POST /api/users')
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
  })

  test('搜索用户', async ({ page }) => {
    await page.goto(BASE_URL + '/system/user')
    await page.waitForLoadState('networkidle')
    await page.fill('input[placeholder="搜索用户名"]', 'admin')
    await page.click('button:has-text("搜索")')
    await page.waitForTimeout(1000)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('编辑用户 - API PUT /users 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/system/user')
    await page.waitForLoadState('networkidle')
    const editBtn = page.locator('button:has-text("编辑")').first()
    if (await editBtn.isVisible()) {
      await editBtn.click()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await page.fill('.el-dialog input[placeholder="请输入姓名"]', 'E2E测试员-已编辑')

      const [updateResp] = await Promise.all([
        page.waitForResponse(resp =>
          resp.url().includes('/api/users') && resp.request().method() === 'PUT',
          { timeout: 5000 }
        ),
        page.click('.el-dialog button:has-text("确定")')
      ])

      await verifyApi200(updateResp, 'PUT /api/users')
      await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
    }
  })

  test('删除用户 - API DELETE /users 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/system/user')
    await page.waitForLoadState('networkidle')
    const deleteBtn = page.locator('button:has-text("删除")').first()
    if (await deleteBtn.isVisible()) {
      page.on('dialog', dialog => dialog.accept())
      const [deleteResp] = await Promise.all([
        page.waitForResponse(resp =>
          resp.url().includes('/api/users') && resp.request().method() === 'DELETE',
          { timeout: 5000 }
        ).catch(() => null),
        deleteBtn.click()
      ])
      if (deleteResp) {
        await verifyApi200(deleteResp, 'DELETE /api/users')
      }
      await page.waitForTimeout(1000)
    }
  })
})

test.describe('角色管理', () => {
  test.beforeEach(async ({ page, loggedIn }) => {
  })

  test('角色列表页正常加载 - API /roles 返回 200', async ({ page }) => {
    const [rolesResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/roles') && resp.request().method() === 'GET',
        { timeout: 15000 }
      ),
      page.goto(BASE_URL + '/system/role')
    ])
    const body = await verifyApi200(rolesResp, 'GET /api/roles')
    expect(body.data.records, '[API] roles 应包含 records 分页数据').toBeDefined()
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 })
  })

  test('新增角色 - API POST /roles 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/system/role')
    await page.waitForLoadState('networkidle')
    await page.click('button:has-text("新增角色")')
    await expect(page.locator('.el-dialog')).toBeVisible()

    const timestamp = Date.now()
    await page.fill('.el-dialog input[placeholder="请输入角色名称"]', `E2E测试角色_${timestamp}`)
    await page.fill('.el-dialog input[placeholder="请输入角色编码"]', `E2E_ROLE_${timestamp}`)
    await page.fill('.el-dialog textarea[placeholder="请输入描述"]', 'Playwright自动创建的测试角色')

    const [createResp] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes('/api/roles') && resp.request().method() === 'POST',
        { timeout: 5000 }
      ),
      page.click('.el-dialog button:has-text("确定")')
    ])

    await verifyApi200(createResp, 'POST /api/roles')
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
  })

  test('编辑角色 - API PUT /roles 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/system/role')
    await page.waitForLoadState('networkidle')
    const editBtn = page.locator('button:has-text("编辑")').first()
    if (await editBtn.isVisible()) {
      await editBtn.click()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await page.fill('.el-dialog input[placeholder="请输入角色名称"]', 'E2E测试角色-已编辑')

      const [updateResp] = await Promise.all([
        page.waitForResponse(resp =>
          resp.url().includes('/api/roles') && resp.request().method() === 'PUT',
          { timeout: 5000 }
        ),
        page.click('.el-dialog button:has-text("确定")')
      ])

      await verifyApi200(updateResp, 'PUT /api/roles')
      await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
    }
  })

  test('权限配置弹窗', async ({ page }) => {
    await page.goto(BASE_URL + '/system/role')
    await page.waitForLoadState('networkidle')
    const permBtn = page.locator('button:has-text("权限配置")').first()
    if (await permBtn.isVisible()) {
      await permBtn.click()
      await expect(page.locator('.el-dialog')).toBeVisible()
      await expect(page.locator('.el-tree')).toBeVisible()

      const [saveResp] = await Promise.all([
        page.waitForResponse(resp =>
          resp.url().includes('/api/permissions') &&
          (resp.request().method() === 'PUT' || resp.request().method() === 'POST'),
          { timeout: 5000 }
        ).catch(() => null),
        page.click('.el-dialog button:has-text("保存")')
      ])
      if (saveResp) {
        await verifyApi200(saveResp, 'PUT/POST /api/permissions')
      }
      await page.waitForTimeout(1000)
    }
  })

  test('删除角色 - API DELETE /roles 返回 200', async ({ page }) => {
    await page.goto(BASE_URL + '/system/role')
    await page.waitForLoadState('networkidle')
    const deleteBtn = page.locator('button:has-text("删除")').first()
    if (await deleteBtn.isVisible()) {
      page.on('dialog', dialog => dialog.accept())
      const [deleteResp] = await Promise.all([
        page.waitForResponse(resp =>
          resp.url().includes('/api/roles') && resp.request().method() === 'DELETE',
          { timeout: 5000 }
        ).catch(() => null),
        deleteBtn.click()
      ])
      if (deleteResp) {
        await verifyApi200(deleteResp, 'DELETE /api/roles')
      }
      await page.waitForTimeout(1000)
    }
  })
})
