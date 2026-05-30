import { test as base, expect } from '@playwright/test'
import { env } from './config/env'

const BASE_URL = env.BASE_URL

type Fixtures = {
  loggedIn: void
}

const test = base.extend<Fixtures>({
  loggedIn: async ({ page }, use) => {
    await page.goto(BASE_URL + env.LOGIN_PAGE)
    await page.fill('input[placeholder="请输入用户名"]', env.DEFAULT_USER.username)
    await page.fill('input[placeholder="请输入密码"]', env.DEFAULT_USER.password)
    await page.click('button:has-text("登录")')
    await page.waitForURL('**/dashboard', { timeout: env.API_TIMEOUT })
    await use()
  },
})

export { test, expect, BASE_URL, env }
