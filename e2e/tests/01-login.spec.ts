import { test, expect, BASE_URL, env } from './fixtures'
import { verifyApi200 } from './utils/api-helper'

test.describe('登录与认证', () => {
  test('页面加载正常', async ({ page }) => {
    await page.goto(BASE_URL + env.LOGIN_PAGE)
    await expect(page.locator('.login-card')).toBeVisible({ timeout: env.API_TIMEOUT })
    await expect(page.locator('input[placeholder="请输入用户名"]')).toBeVisible()
    await expect(page.locator('input[placeholder="请输入密码"]')).toBeVisible()
    await expect(page.locator('button:has-text("登录")')).toBeVisible()
  })

  test('空表单校验', async ({ page }) => {
    await page.goto(BASE_URL + env.LOGIN_PAGE)
    await page.click('button:has-text("登录")')
    await expect(page.locator('.el-form-item__error').first()).toBeVisible({ timeout: 3000 })
  })

  test('错误密码登录失败 - API 返回 401', async ({ page }) => {
    await page.goto(BASE_URL + env.LOGIN_PAGE)
    await page.fill('input[placeholder="请输入用户名"]', env.DEFAULT_USER.username)
    await page.fill('input[placeholder="请输入密码"]', 'wrongpassword')

    const [response] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes(env.API_PATTERNS.AUTH_LOGIN) && resp.request().method() === 'POST',
        { timeout: env.DIALOG_TIMEOUT }
      ),
      page.click('button:has-text("登录")')
    ])

    expect(response.status()).toBe(401, '[API] 错误密码应返回 HTTP 401')
    const body = await response.json()
    expect(body.code).toBe(401)
  })

  test('正确账号密码登录成功 - API 返回 200 且含 token', async ({ page }) => {
    await page.goto(BASE_URL + env.LOGIN_PAGE)
    await page.fill('input[placeholder="请输入用户名"]', env.DEFAULT_USER.username)
    await page.fill('input[placeholder="请输入密码"]', env.DEFAULT_USER.password)

    const [response] = await Promise.all([
      page.waitForResponse(resp =>
        resp.url().includes(env.API_PATTERNS.AUTH_LOGIN) && resp.request().method() === 'POST'
      ),
      page.click('button:has-text("登录")')
    ])

    const body = await verifyApi200(response, 'POST /api/auth/login')
    expect(body.data.token, '[API] 登录响应应包含 token').toBeTruthy()
    expect(body.data.username).toBe(env.DEFAULT_USER.username)

    await page.waitForURL('**/dashboard', { timeout: env.API_TIMEOUT })
    await expect(page).toHaveURL(/dashboard/)
  })
})
