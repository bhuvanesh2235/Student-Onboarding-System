# ============================================================
#  run-selenium-tests.ps1
#  Starts the React frontend (if not already running),
#  runs SignupTest then LoginTest, then cleans up.
#
#  Prerequisites (must be running BEFORE this script):
#    - PostgreSQL
#    - Spring Boot backend on port 8080:
#        cd student-service && ./mvnw spring-boot:run
#
#  Usage (from ANY directory):
#    ..\run-selenium-tests.ps1    # from student-service/
#    .\run-selenium-tests.ps1     # from project root
# ============================================================

$Root        = $PSScriptRoot
$FrontendDir = Join-Path $Root "frontend"
$BackendDir  = Join-Path $Root "student-service"
$MaxWaitSec  = 90
$startedNpm  = $false

# Helper: check if a TCP port is open
function Test-Port($port) {
    $r = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue
    return $r.TcpTestSucceeded
}

# Check backend is up first
if (-not (Test-Port 8080)) {
    Write-Host "[ERROR] Backend not running on port 8080." -ForegroundColor Red
    Write-Host "  Start it: cd student-service && ./mvnw spring-boot:run" -ForegroundColor Yellow
    exit 1
}
Write-Host "[OK] Backend is up on port 8080." -ForegroundColor Green

# Start frontend if not already running
if (Test-Port 3000) {
    Write-Host "[OK] Frontend already running on port 3000." -ForegroundColor Green
} else {
    Write-Host "[1] Starting React frontend..." -ForegroundColor Cyan
    $npmJob = Start-Process -FilePath "cmd.exe" `
        -ArgumentList "/c set BROWSER=none&& npm start" `
        -WorkingDirectory $FrontendDir `
        -NoNewWindow -PassThru
    $startedNpm = $true

    Write-Host "[2] Waiting for frontend on port 3000..." -ForegroundColor Cyan
    $elapsed = 0
    while ($elapsed -lt $MaxWaitSec) {
        if (Test-Port 3000) { break }
        Start-Sleep -Seconds 3
        $elapsed += 3
        Write-Host "  ...waiting ($elapsed s)" -ForegroundColor DarkGray
    }
    if (-not (Test-Port 3000)) {
        Write-Host "[ERROR] Frontend did not start in $MaxWaitSec s." -ForegroundColor Red
        Stop-Process -Id $npmJob.Id -Force -ErrorAction SilentlyContinue
        exit 1
    }
    Write-Host "[OK] Frontend is up!" -ForegroundColor Green
    Start-Sleep -Seconds 2
}

# Run SignupTest
Write-Host "`n[3] Running SignupTest..." -ForegroundColor Cyan
Set-Location $BackendDir
.\mvnw test -Dtest=SignupTest
$signupExit = $LASTEXITCODE

# Run LoginTest
Write-Host "`n[4] Running LoginTest..." -ForegroundColor Cyan
.\mvnw test -Dtest=LoginTest
$loginExit = $LASTEXITCODE
Set-Location $Root

# Stop frontend if we started it
if ($startedNpm) {
    Write-Host "`nStopping React frontend..." -ForegroundColor Cyan
    Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess |
        ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
}

# Summary
Write-Host "`n========== SELENIUM TEST SUMMARY ==========" -ForegroundColor Yellow
if ($signupExit -eq 0) { Write-Host "  SignupTest : PASSED" -ForegroundColor Green }
else                   { Write-Host "  SignupTest : FAILED (exit $signupExit)" -ForegroundColor Red }
if ($loginExit  -eq 0) { Write-Host "  LoginTest  : PASSED" -ForegroundColor Green }
else                   { Write-Host "  LoginTest  : FAILED (exit $loginExit)" -ForegroundColor Red }
Write-Host "==========================================`n" -ForegroundColor Yellow

exit ([Math]::Max($signupExit, $loginExit))
