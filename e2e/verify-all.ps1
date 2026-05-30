$loginBody = @{username='admin';password='admin123'} | ConvertTo-Json
$loginRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -Body $loginBody -ContentType 'application/json'
$token = $loginRes.data.token
$headers = @{ Authorization = "Bearer $token" }
$passed = 0
$failed = 0

function Test-Api($name, $url, $method='GET', $body=$null) {
  try {
    if ($method -eq 'GET') {
      $res = Invoke-WebRequest -Uri $url -Method GET -Headers $headers
    } else {
      $res = Invoke-WebRequest -Uri $url -Method $method -Body ($body | ConvertTo-Json) -ContentType 'application/json' -Headers $headers
    }
    if ($res.StatusCode -eq 200) {
      Write-Host "  PASS: $name" -ForegroundColor Green
      $script:passed++
    } else {
      Write-Host "  FAIL: $name" -ForegroundColor Red
      $script:failed++
    }
  } catch {
    Write-Host "  FAIL: $name" -ForegroundColor Red
    $script:failed++
  }
}

Write-Host ''
Write-Host '===== Phase 1: Basic Functions =====' -ForegroundColor Cyan
Test-Api 'Login' 'http://localhost:8080/api/auth/info'
Test-Api 'UserList' 'http://localhost:8080/api/users?current=1&size=10'
Test-Api 'RoleList' 'http://localhost:8080/api/roles?current=1&size=10'
Test-Api 'PermList' 'http://localhost:8080/api/permissions'
Test-Api 'ProjList' 'http://localhost:8080/api/projects?current=1&size=10'
Test-Api 'CaseList' 'http://localhost:8080/api/testcases?current=1&size=10'
Test-Api 'ExecList' 'http://localhost:8080/api/executions?current=1&size=10'
Test-Api 'RptList' 'http://localhost:8080/api/reports?current=1&size=10'
Test-Api 'Dashboard' 'http://localhost:8080/api/reports/dashboard'
Test-Api 'Trend' 'http://localhost:8080/api/reports/trend?days=7'
Test-Api 'RolePerm' 'http://localhost:8080/api/roles/1/permissions'

Write-Host ''
Write-Host '===== Phase 2: Automation Features =====' -ForegroundColor Cyan
Test-Api 'EnvList' 'http://localhost:8080/api/environments?current=1&size=10'
Test-Api 'JenkinsList' 'http://localhost:8080/api/jenkins/configs?projectId=1'
Test-Api 'CreateEnv' 'http://localhost:8080/api/environments' 'POST' @{projectId=1;name='APITestEnv';type='DEV';url='https://dev-api.example.com';description='API created'}
Test-Api 'CreateJenkins' 'http://localhost:8080/api/jenkins/configs' 'POST' @{projectId=1;name='APIJenkins';jenkinsUrl='http://jenkins-api.example.com';username='admin';apiToken='api-token-123';jobName='api-test-job';status=1}
Test-Api 'ExecStatus' 'http://localhost:8080/api/executions/1/status'

Write-Host ''
Write-Host '===== Results =====' -ForegroundColor Cyan
Write-Host "Passed: $passed  Failed: $failed  Total: $($passed + $failed)"
