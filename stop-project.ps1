# ============================================================
#  AutoEcole SaaS – Full Stack Stop Script
#  Stops: Frontend (node) → Backend (java) → DB Container
# ============================================================

$BackendDir = "C:\Users\youss\autoecole-saas-backend"

function Write-OK($msg)   { Write-Host "  ✔  $msg" -ForegroundColor Green }
function Write-Info($msg) { Write-Host "  →  $msg" -ForegroundColor Cyan }

Clear-Host
Write-Host ""
Write-Host "  ╔══════════════════════════════════════════════╗" -ForegroundColor Red
Write-Host "  ║      AutoEcole SaaS – Stopping Services      ║" -ForegroundColor Red
Write-Host "  ╚══════════════════════════════════════════════╝" -ForegroundColor Red
Write-Host ""

# ── Stop Frontend (node / ng serve) ──────────────────────────
Write-Info "Stopping Frontend (Angular / node)..."
$nodeProcs = Get-Process -Name "node" -ErrorAction SilentlyContinue
if ($nodeProcs) {
    $nodeProcs | Stop-Process -Force
    Write-OK "Frontend process(es) stopped."
} else {
    Write-Host "  ⚠  No node process found (already stopped)." -ForegroundColor DarkYellow
}

# ── Stop Backend (java / Spring Boot) ────────────────────────
Write-Info "Stopping Backend (Spring Boot / java)..."
$javaProcs = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcs) {
    $javaProcs | Stop-Process -Force
    Write-OK "Backend process(es) stopped."
} else {
    Write-Host "  ⚠  No java process found (already stopped)." -ForegroundColor DarkYellow
}

# ── Stop Docker DB ────────────────────────────────────────────
Write-Info "Stopping Database container (PostgreSQL)..."
Set-Location $BackendDir
docker-compose down
if ($LASTEXITCODE -eq 0) {
    Write-OK "Database container stopped."
} else {
    Write-Host "  ⚠  docker-compose down reported an issue." -ForegroundColor DarkYellow
}

# ── Done ──────────────────────────────────────────────────────
Write-Host ""
Write-Host "  ╔══════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "  ║        All services stopped cleanly.         ║" -ForegroundColor Green
Write-Host "  ╚══════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Read-Host "  Press Enter to close"
