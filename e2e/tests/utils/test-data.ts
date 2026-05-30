export function generateUniqueId(prefix: string = 'E2E'): string {
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 100000)
  return `${prefix}_${timestamp}_${random}`
}

export function generateTestCaseName(type: 'UI' | 'API' = 'UI'): string {
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 1000)
  if (type === 'UI') {
    return `E2E登录功能测试_${timestamp}_${random}`
  }
  return `E2E登录接口测试_${timestamp}_${random}`
}

export function generateProjectName(): string {
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 10000)
  return {
    name: `E2E测试项目_${timestamp}`,
    code: `e2e-${timestamp}-${random}`
  }
}

export function generateUserName(): string {
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 1000)
  return `e2e_tester_${timestamp}_${random}`
}

export function generateRoleName(): string {
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 1000)
  return {
    name: `E2E测试角色_${timestamp}`,
    code: `E2E_ROLE_${timestamp}-${random}`
  }
}
