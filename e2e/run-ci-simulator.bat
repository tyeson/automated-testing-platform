@echo off
REM ============================================================
REM GitHub Actions CI Simulator - Quick Launcher
REM ============================================================

echo.
echo ══════════════════════════════════════════════════════
echo   🚀 GitHub Actions E2E CI Simulator - Quick Start
echo ══════════════════════════════════════════════════════
echo.

REM 检查 PowerShell 版本
for /f "tokens=*" %%v in ('powershell -Command "$PSVersionTable.PSVersion.ToString()"') do set PS_VER=%%v

echo [INFO] PowerShell Version: %PS_VER%
echo.

REM 显示菜单
echo Please select a mode:
echo   1. Quick Smoke Test (3 suites, ~5 min)
echo   2. Full Regression (61 tests, ~10 min)  
echo   3. Custom Tests (specify files)
echo   4. Environment Check Only
echo   0. Exit
echo.

set /p CHOICE="Enter your choice (0-4): "

if "%CHOICE%"=="1" (
    echo.
    echo [▶] Starting QUICK smoke test...
    powershell -ExecutionPolicy Bypass -File "%~dp0simulate-ci.ps1" -Mode Quick
) else if "%CHOICE%"=="2" (
    echo.
    echo [▶] Starting FULL regression test...
    powershell -ExecutionPolicy Bypass -File "%~dp0simulate-ci.ps1" -Mode Full
) else if "%CHOICE%"=="3" (
    echo.
    set /p FILES="Enter test files (comma-separated, e.g., 01-login.spec.ts,04-testcase.spec.ts): "
    echo.
    echo [▶] Starting CUSTOM tests...
    powershell -ExecutionPolicy Bypass -File "%~dp0simulate-ci.ps1" -Mode Custom -TestFiles %FILES%
) else if "%CHOICE%"=="4" (
    echo.
    echo [▶] Running ENVIRONMENT CHECK only...
    powershell -ExecutionPolicy Bypass -File "%~dp0simulate-ci.ps1" -Mode Quick -SkipEnvCheck:$false
) else if "%CHOICE%"=="0" (
    echo.
    echo [✋] Exiting...
    goto :eof
) else (
    echo.
    echo [❌] Invalid choice. Please enter 0-4.
    pause
    goto :eof
)

echo.
echo ══════════════════════════════════════════════════════
echo   Simulation Complete!
echo   Report: e2e\ci-report.html
echo   Log:    e2e\ci-simulation.log
echo ══════════════════════════════════════════════════════
echo.

pause
