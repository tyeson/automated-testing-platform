import { expect, type Response } from '@playwright/test'

export async function verifyApi200(response: Response, apiName: string) {
  expect(response.status()).toBe(200, `[API] ${apiName} HTTP 应为 200`)
  const body = await response.json()
  expect(body.code).toBe(200, `[API] ${apiName} 业务码应为 200`)
  return body
}

export async function waitForApiResponse(
  page: import('@playwright/test').Page,
  urlPattern: string | RegExp,
  method: string = 'GET',
  options: { timeout?: number } = {}
): Promise<Response | null> {
  const { timeout = 10000 } = options
  return page.waitForResponse(
    resp => resp.url().includes(urlPattern.toString()) && resp.request().method() === method,
    { timeout }
  ).catch(() => null)
}

export async function waitForPageLoadWithApi(
  page: import('@playwright/test').Page,
  url: string,
  apiPattern: string,
  method: string = 'GET'
) {
  return Promise.all([
    page.waitForResponse(
      resp => resp.url().includes(apiPattern) && resp.request().method() === method,
      { timeout: 15000 }
    ).catch(() => null),
    page.goto(url)
  ])
}
