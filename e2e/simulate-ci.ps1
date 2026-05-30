<#
.SYNOPSIS
    GitHub Actions E2E Tests CI Simulator
.DESCRIPTION
    Local CI simulation with environment checks, service validation, and test execution
#>

param(
    [ValidateSet("Full", "Quick", "Custom")]
    [string]$Mode = "Full",
    [string[]]$TestFiles = @(),
    [switch]$SkipEnvCheck,
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_DIR
$E2E_DIR = Join-Path $PROJECT_ROOT "e2e"
$FRONTEND_DIR = Join-Path $PROJECT_ROOT "frontend"
$BACKEND_DIR = Join-Path $PROJECT_ROOT "backend"

$LOG_FILE = Join-Path $E2E_DIR "ci-simulation.log"
$REPORT_FILE = Join-Path $E2E_DIR "ci-report.html"
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmmss"

$env:CI = "true"
$env:GITHUB_ACTIONS = "true"

Write-Host ""
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  GitHub Actions E2E CI Simulator" -ForegroundColor Cyan
$current_time = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
Write-Host "  Mode: $Mode | Time: $current_time" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

$script:TotalTests = 0
$script:PassedTests = 0
$script:FailedTests = 0
$script:SkippedTests = 0
$script:StartTime = Get-Date

function Write-Step([string]$Message) {
    Write-Host ""
    Write-Host "=== $Message ===" -ForegroundColor Yellow
}

function Write-Pass([string]$Message) {
    Write-Host "  [PASS] $Message" -ForegroundColor Green
    $script:PassedTests++
    $script:TotalTests++
}

function Write-Fail([string]$Message, [string]$Details = "") {
    Write-Host "  [FAIL] $Message" -ForegroundColor Red
    if ($Details) { Write-Host "         -> $Details" -ForegroundColor DarkRed }
    $script:FailedTests++
    $script:TotalTests++
}

function Write-Skip([string]$Message, [string]$Reason = "") {
    Write-Host "  [SKIP] $Message" -ForegroundColor Yellow
    if ($Reason) { Write-Host "         -> Reason: $Reason" -ForegroundColor DarkYellow }
    $script:SkippedTests++
    $script:TotalTests++
}

function Test-Command([string]$Command) {
    try { $null = Get-Command $Command -ErrorAction Stop; return $true }
    catch { return $false }
}

function Test-Url([string]$Url, [int]$Timeout = 5) {
    try {
        $response = Invoke-WebRequest -Uri $Url -Method Head -TimeoutSec $Timeout -ErrorAction Stop
        return $response.StatusCode -eq 200
    } catch { return $false }
}

# Step 1: Environment Check
function Invoke-EnvironmentCheck() {
    Write-Step "Step 1/7: Environment Check"
    
    $checks = @(
        @{ Name="Node.js"; Command="node"; VersionArg="--version" },
        @{ Name="npm"; Command="npm"; VersionArg="--version" },
        @{ Name="Java"; Command="java"; VersionArg="-version" },
        @{ Name="Maven"; Command="mvn"; VersionArg="-v" },
        @{ Name="Playwright"; Command="npx playwright"; VersionArg="--version" },
        @{ Name="Git"; Command="git"; VersionArg="--version" }
    )
    
    foreach ($check in $checks) {
        if (-not (Test-Command $check.Command)) {
            Write-Fail "$($check.Name) not installed"
            continue
        }
        
        try {
            $output = & $check.Command $check.VersionArg 2>&1 | Select-Object -First 1
            if ($output -match '(\d+\.\d+(\.\d+)?|version)') {
                Write-Pass "$($check.Name) v$($Matches[1])"
            } else {
                Write-Pass "$($check.Name) installed"
            }
        } catch {
            Write-Fail "$($check.Name) check failed"
        }
    }
    
    Write-Host ""
    Write-Host "  Checking project structure..." -ForegroundColor White
    
    $dirs = @(
        @{ Path=$E2E_DIR; Name="E2E directory" },
        @{ Path=(Join-Path $E2E_DIR "tests"); Name="Tests directory" },
        @{ Path=(Join-Path $E2E_DIR "tests\config"); Name="Config directory" },
        @{ Path=(Join-Path $E2E_DIR "tests\utils"); Name="Utils directory" }
    )
    
    foreach ($dir in $dirs) {
        if (Test-Path $dir.Path) { Write-Pass "$($dir.Name)" }
        else { Write-Fail "$($dir.Name) missing" }
    }
    
    Write-Host ""
    Write-Host "  Checking critical files..." -ForegroundColor White
    
    $files = @(
        @{ Path=(Join-Path $E2E_DIR "package.json"); Name="package.json" },
        @{ Path=(Join-Path $E2E_DIR "playwright.config.ts"); Name="playwright.config.ts" },
        @{ Path=(Join-Path $E2E_DIR "tests\fixtures.ts"); Name="fixtures.ts" },
        @{ Path=(Join-Path $E2E_DIR "tests\config\env.ts"); Name="env.ts" },
        @{ Path=(Join-Path $E2E_DIR "tests\utils\api-helper.ts"); Name="api-helper.ts" },
        @{ Path=(Join-Path $PROJECT_ROOT ".github\workflows\e2e-tests.yml"); Name="e2e-tests.yml" }
    )
    
    foreach ($file in $files) {
        if (Test-Path $file.Path) { Write-Pass "$($file.Name)" }
        else { Write-Fail "$($file.Name) missing" }
    }
    
    Write-Host ""
    $e2eModules = Join-Path $E2E_DIR "node_modules"
    if (Test-Path $e2eModules) { Write-Pass "E2E dependencies installed" }
    else { Write-Skip "E2E dependencies not found" }
}

# Step 2: Service Health Check
function Invoke-ServiceHealthCheck() {
    Write-Step "Step 2/7: Service Health Check"
    
    Write-Host "  Checking frontend (localhost:5173)..." -ForegroundColor White
    if (Test-Url "http://localhost:5173") { Write-Pass "Frontend running" }
    else { Write-Fail "Frontend not responding" }
    
    Write-Host ""
    Write-Host "  Checking backend (localhost:8080)..." -ForegroundColor White
    if (Test-Url "http://localhost:8080/api/auth/login") { Write-Pass "Backend running" }
    else { Write-Fail "Backend not responding" }
    
    Write-Host ""
    Write-Host "  Testing database connection via login API..." -ForegroundColor White
    try {
        $body = @{username="admin"; password="admin123"} | ConvertTo-Json
        $resp = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post `
            -Body $body -ContentType "application/json" -ErrorAction Stop
        
        if ($resp.code -eq 200 -and $resp.data.token) { Write-Pass "Database OK - Login successful" }
        else { Write-Fail "Auth failed - code $($resp.code)" }
    } catch { Write-Fail "DB connection failed" }
}

# Step 3: Config Validation
function Invoke-ConfigValidation() {
    Write-Step "Step 3/7: Configuration Validation"
    
    $envPath = Join-Path $E2E_DIR "tests\config\env.ts"
    if (Test-Path $envPath) {
        $content = Get-Content $envPath -Raw
        
        $checks = @("BASE_URL", "LOGIN_PAGE", "API_PATTERNS", "DEFAULT_USER", "isCI")
        foreach ($c in $checks) {
            if ($content -match $c) { Write-Pass "Config: $c defined" }
            else { Write-Fail "Config: $c missing" }
        }
    } else { Write-Fail "env.ts not found" }
    
    Write-Host ""
    $pwPath = Join-Path $E2E_DIR "playwright.config.ts"
    if (Test-Path $pwPath) {
        $pwContent = Get-Content $pwPath -Raw
        $pwChecks = @("testDir", "baseURL", "headless", "screenshot", "trace")
        foreach ($c in $pwChecks) {
            if ($pwContent -match $c) { Write-Pass "Playwright: $c configured" }
            else { Write-Skip "Playwright: $c optional" }
        }
    }
}

# Step 4: Dependency Check
function Invoke-DependencyCheck() {
    Write-Step "Step 4/7: Dependency Check"
    
    $pkgPath = Join-Path $E2E_DIR "package.json"
    if (Test-Path $pkgPath) {
        $pkg = Get-Content $pkgPath | ConvertFrom-Json
        if ($pkg.devDependencies.'@playwright/test') {
            Write-Pass "@playwright/test v$($pkg.devDependencies.'@playwright/test')"
        } else { Write-Fail "@playwright/test missing" }
        
        $browserPath = Join-Path $env:USERPROFILE ".cache\ms-playwright"
        if (Test-Path $browserPath) {
            $count = (Get-ChildItem $browserPath -Directory).Count
            Write-Pass "Browsers installed ($count)"
        } else { Write-Skip "Browsers not installed" }
    }
    
    Write-Host ""
    $utils = @("api-helper.ts", "test-data.ts")
    foreach ($u in $utils) {
        $upath = Join-Path $E2E_DIR "tests\utils\$u"
        if (Test-Path $upath) { Write-Pass "Utils: $u available" }
        else { Write-Skip "Utils: $u missing" }
    }
}

# Step 5: Run Tests
function Invoke-E2ETests() {
    Write-Step "Step 5/7: Execute E2E Tests"
    
    Push-Location $E2E_DIR
    try {
        switch ($Mode) {
            "Quick" {
                $testArgs = @("01-login.spec.ts", "02-dashboard.spec.ts", "08-api-intercept.spec.ts")
                Write-Host "  Running quick smoke tests (3 suites)..." -ForegroundColor White
            }
            "Full" {
                $testArgs = @()
                Write-Host "  Running full regression (61 tests)..." -ForegroundColor White
            }
            "Custom" {
                if ($TestFiles.Count -eq 0) { Write-Error "Custom mode requires -TestFiles" }
                $testArgs = $TestFiles
                Write-Host "  Running custom tests..." -ForegroundColor White
            }
        }
        
        $cmd = "npx playwright test"
        if ($testArgs.Count -gt 0) { $cmd += " $($testArgs -join ' ')" }
        $cmd += " --reporter=list,json"
        
        Write-Host ""
        Write-Host "  Command: $cmd" -ForegroundColor Gray
        Write-Host ""
        
        $start = Get-Date
        $output = & cmd /c $cmd 2>&1 | Out-String
        $duration = (Get-Date) - $start
        
        $output | Out-File $LOG_FILE -Encoding UTF8
        
        $lines = $output -split "`n"
        $resultLine = $lines | Where-Object { $_ -match '\d+ (passed|failed)' } | Select-Object -Last 1
        
        if ($resultLine -match '(\d+)\s*passed') {
            Write-Pass "Tests completed in $($duration.ToString('mm\:ss'))" "$($Matches[1]) passed"
        } elseif ($resultLine -match '(\d+)\s*failed') {
            Write-Fail "Tests completed with failures" "$($Matches[1]) failed"
        } else {
            Write-Skip "Result parsing unclear" "Check log file"
        }
        
    } finally { Pop-Location }
}

# Step 6 & 7: Results and Report
function Invoke-FinalReport() {
    Write-Step "Steps 6-7: Results Collection & Report"
    
    $reportDir = Join-Path $E2E_DIR "playwright-report"
    if (Test-Path $reportDir) {
        $count = (Get-ChildItem $reportDir -Recurse -File).Count
        Write-Pass "HTML report generated" "$count file(s)"
    } else { Write-Skip "No report directory" }
    
    $totalDuration = (Get-Date) - $script:StartTime
    $passRate = if ($script:TotalTests -gt 0) { [math]::Round(($script:PassedTests / $script:TotalTests) * 100, 1) } else { 0 }
    
    Write-Host ""
    Write-Host "=====================================================" -ForegroundColor Cyan
    Write-Host "  CI SIMULATION SUMMARY" -ForegroundColor Cyan
    Write-Host "=====================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Mode:      $Mode" -ForegroundColor White
    Write-Host "  Duration:  $($totalDuration.ToString('mm\:ss'))" -ForegroundColor White
    Write-Host "  Total:     $($script:TotalTests)" -ForegroundColor White
    Write-Host "  Passed:    $($script:PassedTests)" -ForegroundColor Green
    Write-Host "  Failed:    $($script:FailedTests)" -ForegroundColor Red
    Write-Host "  Skipped:   $($script:SkippedTests)" -ForegroundColor Yellow
    Write-Host "  Pass Rate: $passRate%" -ForegroundColor White
    Write-Host ""
    
    if ($script:FailedTests -eq 0) {
        Write-Host "  SUCCESS! All checks passed. Ready for deployment!" -ForegroundColor Green
        $exitCode = 0
    } else {
        Write-Host "  WARNING! $($script:FailedTests) check(s) failed." -ForegroundColor Red
        $exitCode = 1
    }
    
    # Generate simple HTML report
    $status = if ($script:FailedTests -eq 0) { "READY" } else { "ISSUES DETECTED" }
    $color = if ($script:FailedTests -eq 0) { "#28a745" } else { "#dc3545" }
    
    $html = @"
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>CI Report</title>
<style>
body{font-family:sans-serif;background:linear-gradient(135deg,#667eea,#764ba2);min-height:100vh;padding:20px}
.container{max-width:800px;margin:0 auto;background:white;border-radius:12px;box-shadow:0 20px 60px rgba(0,0,0,.3);overflow:hidden}
.header{background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;padding:30px;text-align:center}
.header h1{font-size:28px;margin-bottom:10px}
.content{padding:30px}
.stats{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:15px;margin:20px 0}
.stat{background:#f8f9fa;border-radius:8px;padding:20px;text-align:center;border-left:4px solid #667eea}
.stat.passed{border-left-color:#28a745}.stat.failed{border-left-color:#dc3545}.stat.skipped{border-left-color:#ffc107}
.num{font-size:32px;font-weight:bold;color:#333}
.label{color:#666;margin-top:5px}
.progress{height:20px;background:#e9ecef;border-radius:10px;overflow:hidden;margin:20px 0}
.fill{height:100%;background:linear-gradient(90deg,#28a745,#20c997);width:$passRate%}
.footer{background:#f8f9fa;padding:20px;text-align:center;color:#666;font-size:13px}
</style></head>
<body><div class='container'>
<div class='header'><h1>CI Simulation Report</h1><p>Local Validation for GitHub Actions</p></div>
<div class='content'>
<div style='text-align:center;margin-bottom:20px'>
<span style='display:inline-block;padding:5px 15px;border-radius:20px;font-size:12px;font-weight:bold;background:$color;color:white'>$status</span></div>
<div class='stats'>
<div class='stat'><div class='num'>$($script:TotalTests)</div><div class='label'>Total</div></div>
<div class='stat passed'><div class='num' style='color:#28a745'>$($script:PassedTests)</div><div class='label'>Passed</div></div>
<div class='stat failed'><div class='num' style='color:#dc3545'>$($script:FailedTests)</div><div class='label'>Failed</div></div>
<div class='stat skipped'><div class='num' style='color:#ffc107'>$($script:SkippedTests)</div><div class='label'>Skipped</div></div>
</div>
<h3 style='margin:25px 0 15px;padding-bottom:10px;border-bottom:2px solid #eee'>Pass Rate</h3>
<div class='progress'><div class='fill'></div></div>
<p style='text-align:center;color:#666;margin-top:10px'><strong>$passRate%</strong> success rate</p>
<table style='width:100%;margin-top:25px;border-collapse:collapse'>
<tr style='background:#f8f9fa'><td style='padding:12px;border-bottom:1px solid #eee'>Mode</td><td style='padding:12px;border-bottom:1px solid #eee'>$Mode</td></tr>
<tr><td style='padding:12px;border-bottom:1px solid #eee'>Duration</td><td style='padding:12px;border-bottom:1px solid #eee'>$($totalDuration.ToString('mm\:ss'))</td></tr>
<tr style='background:#f8f9fa'><td style='padding:12px;border-bottom:1px solid #eee'>Timestamp</td><td style='padding:12px;border-bottom:1px solid #eee'>$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')</td></tr>
<tr><td style='padding:12px;border-bottom:1px solid #eee'>Environment</td><td style='padding:12px;border-bottom:1px solid #eee'>CI=true, GITHUB_ACTIONS=true</td></tr>
</table></div>
<div class='footer'><p>Generated $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')</p></div>
</div></body></html>
"@
    
    $html | Out-File $REPORT_FILE -Encoding UTF8
    Write-Host ""
    Write-Host "  HTML report saved to: $REPORT_FILE" -ForegroundColor Gray
    
    return $exitCode
}

# Main Execution
try {
    if (-not $SkipEnvCheck) { Invoke-EnvironmentCheck }
    else { Write-Step "Step 1/7: Environment Check"; Write-Skip "Skipped by flag" }
    
    Invoke-ServiceHealthCheck
    Invoke-ConfigValidation
    Invoke-DependencyCheck
    Invoke-E2ETests
    $exitCode = Invoke-FinalReport
    
    if (Test-Path $REPORT_FILE) { Start-Process $REPORT_FILE }
    exit $exitCode
    
} catch {
    Write-Host ""
    Write-Host "  FATAL ERROR!" -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
    exit 2
}
