import { test, expect, BASE_URL } from './fixtures'

test.describe('API 网络拦截 - 验证所有接口返回 200 且数据结构正确', () => {

  test('POST /api/auth/login 登录接口返回 200 且包含 token', async ({ page }) => {
    await page.goto(BASE_URL + '/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[placeholder="请输入密码"]', 'admin123')

    const responsePromise = page.waitForResponse(
      resp => resp.url().includes('/api/auth/login') && resp.request().method() === 'POST',
      { timeout: 10000 }
    )
    await page.click('button:has-text("登录")')
    const response = await responsePromise

    const body = await response.json()
    expect(response.status()).toBe(200)
    expect(body.code).toBe(200)
    expect(body.data.token).toBeTruthy()
    expect(body.data.username).toBe('admin')
  })

  test('GET /api/reports/dashboard 指标数据返回 200', async ({ loggedIn, page }) => {
    const response = await page.waitForResponse(
      resp => resp.url().includes('/reports/dashboard'),
      { timeout: 10000 }
    )
    const body = await response.json()
    expect(response.status()).toBe(200)
    expect(body.code).toBe(200)
    expect(body.data.projectCount).toBeDefined()
    expect(typeof body.data.successRate).toBe('number')
  })

  test('GET /api/reports/trend 趋势数据返回 200', async ({ loggedIn, page }) => {
    const response = await page.waitForResponse(
      resp => resp.url().includes('/reports/trend'),
      { timeout: 10000 }
    )
    const body = await response.json()
    expect(response.status()).toBe(200)
    expect(body.code).toBe(200)
    expect(Array.isArray(body.data)).toBeTruthy()
  })

  test('GET /api/projects 项目列表返回 200', async ({ loggedIn, page }) => {
    await page.goto(BASE_URL + '/project')
    const response = await page.waitForResponse(
      resp => resp.url().includes('/projects') && resp.request().method() === 'GET',
      { timeout: 10000 }
    )
    const body = await response.json()
    expect(response.status()).toBe(200)
    expect(body.code).toBe(200)
    expect(body.data.records).toBeDefined()
    expect(typeof body.data.total).toBe('number')
  })

  test('GET /api/testcases 用例列表返回 200', async ({ loggedIn, page }) => {
    await page.goto(BASE_URL + '/testcase')
    const response = await page.waitForResponse(
      resp => resp.url().includes('/testcases') && !resp.url().includes('suites'),
      { timeout: 10000 }
    )
    const body = await response.json()
    expect(response.status()).toBe(200)
    expect(body.code).toBe(200)
  })

  test('GET /api/testcases/suites 测试套件返回 200（非 mock 数据）', async ({ loggedIn, page }) => {
    await page.goto(BASE_URL + '/testcase')

    const [response] = await Promise.all([
      page.waitForResponse(resp => resp.url().includes('/testcases/suites')),
      page.click('text=管理套件')
    ])

    const body = await response.json()
    expect(response.status()).toBe(200)
    expect(body.code).toBe(200)
    expect(Array.isArray(body.data)).toBeTruthy()
  })

  test('GET /api/users 用户列表返回 200 且含姓名/手机/角色字段', async ({ loggedIn, page }) => {
    await page.goto(BASE_URL + '/system/user')
    const response = await page.waitForResponse(
      resp => resp.url().includes('/users') && resp.request().method() === 'GET',
      { timeout: 10000 }
    )
    const body = await response.json()
    expect(response.status()).toBe(200)
    expect(body.code).toBe(200)

    if (body.data.records.length > 0) {
      const userWithRole = body.data.records.find((u: any) => u.roleName)
      if (userWithRole) {
        expect(userWithRole.realName).toBeDefined()
        expect(userWithRole.phone).toBeDefined()
        expect(userWithRole.roleName).toBeDefined()
      }
    }
  })

  test('GET /api/roles 角色列表返回 200', async ({ loggedIn, page }) => {
    await page.goto(BASE_URL + '/system/role')
    const response = await page.waitForResponse(
      resp => resp.url().includes('/roles') && resp.request().method() === 'GET',
      { timeout: 10000 }
    )
    const body = await response.json()
    expect(response.status()).toBe(200)
    expect(body.code).toBe(200)
    expect(body.data.records).toBeDefined()
  })

  test('GET /api/reports 报告列表返回 200', async ({ loggedIn, page }) => {
    await page.goto(BASE_URL + '/report')
    const response = await page.waitForResponse(
      resp => resp.url().includes('/reports') && !resp.url().includes('dashboard') && !resp.url().includes('trend'),
      { timeout: 10000 }
    )
    const body = await response.json()
    expect(response.status()).toBe(200)
    expect(body.code).toBe(200)
  })

  test('GET /api/executions 执行列表返回 200', async ({ loggedIn, page }) => {
    await page.goto(BASE_URL + '/execution')
    const response = await page.waitForResponse(
      resp => resp.url().includes('/executions') && resp.request().method() === 'GET',
      { timeout: 10000 }
    )
    const body = await response.json()
    expect(response.status()).toBe(200)
    expect(body.code).toBe(200)
  })

  test('GET /api/environments 环境列表返回 200', async ({ loggedIn, page }) => {
    await page.goto(BASE_URL + '/system/environment')
    const response = await page.waitForResponse(
      resp => resp.url().includes('/environments'),
      { timeout: 10000 }
    )
    const body = await response.json()
    expect(response.status()).toBe(200)
    expect(body.code).toBe(200)
    expect(body.data.records).toBeDefined()
  })

  test('GET /api/jenkins/configs Jenkins配置返回 200', async ({ loggedIn, page }) => {
    await page.goto(BASE_URL + '/system/jenkins')
    const response = await page.waitForResponse(
      resp => resp.url().includes('/jenkins/configs'),
      { timeout: 10000 }
    )
    const body = await response.json()
    expect(response.status()).toBe(200)
    expect(body.code).toBe(200)
    expect(Array.isArray(body.data)).toBeTruthy()
  })
})
