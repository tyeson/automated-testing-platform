# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: 02-dashboard.spec.ts >> 首页工作台 >> Dashboard 页面正常加载 - API /reports/dashboard 返回 200
- Location: tests\02-dashboard.spec.ts:5:7

# Error details

```
Error: page.goto: net::ERR_CONNECTION_REFUSED at http://localhost:5173/login
Call log:
  - navigating to "http://localhost:5173/login", waiting until "load"

```

# Test source

```ts
  1  | import { test as base, expect } from '@playwright/test'
  2  | import { env } from './config/env'
  3  | 
  4  | const BASE_URL = env.BASE_URL
  5  | 
  6  | type Fixtures = {
  7  |   loggedIn: void
  8  | }
  9  | 
  10 | const test = base.extend<Fixtures>({
  11 |   loggedIn: async ({ page }, use) => {
> 12 |     await page.goto(BASE_URL + env.LOGIN_PAGE)
     |                ^ Error: page.goto: net::ERR_CONNECTION_REFUSED at http://localhost:5173/login
  13 |     await page.fill('input[placeholder="请输入用户名"]', env.DEFAULT_USER.username)
  14 |     await page.fill('input[placeholder="请输入密码"]', env.DEFAULT_USER.password)
  15 |     await page.click('button:has-text("登录")')
  16 |     await page.waitForURL('**/dashboard', { timeout: env.API_TIMEOUT })
  17 |     await use()
  18 |   },
  19 | })
  20 | 
  21 | export { test, expect, BASE_URL, env }
  22 | 
```