# ============================================================
#  AutoEcole SaaS – Full Stack Startup Script
#  Starts: PostgreSQL (Docker) → Backend (Spring Boot) → Frontend (Angular)
# ============================================================

$BackendDir  = "C:\Users\youss\autoecole-saas-backend"
$FrontendDir = "C:\Users\youss\autoecole-saas-frontend"

# ── Helpers ──────────────────────────────────────────────────
function Write-Step($num, $msg) {
    Write-Host ""
    Write-Host "  [$num/3] $msg" -ForegroundColor Yellow
}
function Write-OK($msg)   { Write-Host "  ✔  $msg" -ForegroundColor Green }
function Write-Fail($msg) { Write-Host "  ✘  $msg" -ForegroundColor Red }
function Write-Info($msg) { Write-Host "  →  $msg" -ForegroundColor Cyan }

# ── Banner ───────────────────────────────────────────────────
Clear-Host
Write-Host ""
Write-Host "  ╔══════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "  ║      AutoEcole SaaS – Project Launcher       ║" -ForegroundColor Cyan
Write-Host "  ╚══════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# ── Step 1: Start Docker DB ───────────────────────────────────
Write-Step 1 "Starting Database container (PostgreSQL)..."
Set-Location $BackendDir

$dockerRunning = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Fail "Docker Desktop is not running. Please start Docker and try again."
    Read-Host "Press Enter to exit"
    exit 1
}

docker-compose up -d | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Fail "docker-compose failed. Check your docker-compose.yml."
    Read-Host "Press Enter to exit"
    exit 1
}
Write-OK "Container started. Waiting for PostgreSQL to accept connections..."

$maxRetries = 30
$retries    = 0
$ready      = $false
do {
    Start-Sleep -Seconds 2
    $retries++
    $check = docker exec autoecole_db pg_isready -U autoecole_user 2>&1
    if ($check -match "accepting connections") { $ready = $true }
} while (-not $ready -and $retries -lt $maxRetries)

if (-not $ready) {
    Write-Fail "PostgreSQL did not become ready after $($maxRetries * 2) seconds."
    Read-Host "Press Enter to exit"
    exit 1
}
Write-OK "PostgreSQL is ready on port 5432."

# ── Step 2: Start Backend ─────────────────────────────────────
Write-Step 2 "Launching Backend (Spring Boot) in a new terminal..."

$backendCmd = @"
Set-Location '$BackendDir'
`$host.UI.RawUI.WindowTitle = 'AutoEcole – Backend'
Write-Host '  ╔═══════════════════════════════╗' -ForegroundColor Cyan
Write-Host '  ║   AutoEcole Backend (Spring)  ║' -ForegroundColor Cyan
Write-Host '  ╚═══════════════════════════════╝' -ForegroundColor Cyan
Write-Host ''
.\mvnw.cmd spring-boot:run
"@

Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendCmd -WindowStyle Normal
Write-OK "Backend terminal opened. Spring Boot is compiling..."

# ── Step 3: Start Frontend ────────────────────────────────────
Write-Step 3 "Launching Frontend (Angular) in a new terminal..."

$frontendCmd = @"
Set-Location '$FrontendDir'
`$host.UI.RawUI.WindowTitle = 'AutoEcole – Frontend'
Write-Host '  ╔═══════════════════════════════╗' -ForegroundColor Magenta
Write-Host '  ║   AutoEcole Frontend (Angular) ║' -ForegroundColor Magenta
Write-Host '  ╚═══════════════════════════════╝' -ForegroundColor Magenta
Write-Host ''
npm start
"@

Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendCmd -WindowStyle Normal
Write-OK "Frontend terminal opened. Angular dev server is starting..."

# ── Summary ───────────────────────────────────────────────────
Write-Host ""
Write-Host "  ╔══════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "  ║          All services are starting!          ║" -ForegroundColor Green
Write-Host "  ╠══════════════════════════════════════════════╣" -ForegroundColor Green
Write-Host "  ║  🗄  Database   →  localhost:5432             ║" -ForegroundColor Green
Write-Host "  ║  ⚙  Backend    →  http://localhost:8080      ║" -ForegroundColor Green
Write-Host "  ║  🌐 Frontend   →  http://localhost:4200      ║" -ForegroundColor Green
Write-Host "  ╚══════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "  This window can be closed safely." -ForegroundColor Gray
Write-Host ""
Read-Host "  Press Enter to close"
