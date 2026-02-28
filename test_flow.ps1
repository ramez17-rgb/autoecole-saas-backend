$baseUrl = "http://localhost:8080"
$ErrorActionPreference = "SilentlyContinue"

function Call-API {
    param([string]$label, [string]$method, [string]$uri, [hashtable]$headers = @{}, [object]$body = $null)
    Write-Host "`n--- $label ---" -ForegroundColor Cyan
    Write-Host "$method $uri" -ForegroundColor Green
    try {
        $params = @{ Uri = $uri; Method = $method; Headers = $headers }
        if ($body) { $params.Body = ($body | ConvertTo-Json -Depth 5); $params.ContentType = "application/json" }
        $resp = Invoke-RestMethod @params
        Write-Host "SUCCESS: $($resp | ConvertTo-Json -Depth 5)" -ForegroundColor White
        return $resp
    } catch {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $errBody = $reader.ReadToEnd()
        Write-Host "ERROR ($($_.Exception.Response.StatusCode)): $errBody" -ForegroundColor Red
        return $null
    }
}

# ------------------------------------------
# STEP 1: Seed Super Admin (global platform admin)
# ------------------------------------------
Call-API "1. Seed Super Admin" "POST" "$baseUrl/api/auth/seed-super-admin" | Out-Null

# ------------------------------------------
# STEP 2: Register a new Tenant + Admin (the real registration flow)
#         Returns a setupToken to be used for password setup
# ------------------------------------------
$rand = Get-Random -Minimum 1000 -Maximum 9999
$adminEmail = "admin_ecole_$rand@test.com"

$registration = Call-API "2. Register Tenant (Auto-Ecole)" "POST" "$baseUrl/api/tenants/register" -body @{
    name    = "Auto-Ecole Test $rand"
    email   = $adminEmail
    address = "123 Rue du Test, Paris"
}
if (-not $registration) { Write-Host "Tenant registration failed, stopping." -ForegroundColor Red; exit 1 }
$setupToken = $registration.setupToken
Write-Host "Setup Token: $setupToken" -ForegroundColor Yellow

# ------------------------------------------
# STEP 3: Admin sets their password using the setup token
# ------------------------------------------
$adminPassword = "password123"
$pwSetup = Call-API "3. Setup Admin Password" "POST" "$baseUrl/api/auth/setup-password?token=$setupToken" -body @{
    password = $adminPassword
}
if ($pwSetup -eq $null) { Write-Host "Password setup failed, stopping." -ForegroundColor Red; exit 1 }

# ------------------------------------------
# STEP 4: Admin logs in
# ------------------------------------------
$token = Call-API "4. Login as Admin" "POST" "$baseUrl/api/auth/login" -body @{
    email    = $adminEmail
    password = $adminPassword
}
if (-not $token) { Write-Host "Login failed, stopping." -ForegroundColor Red; exit 1 }
$authHeaders = @{ Authorization = "Bearer $token" }
Write-Host "JWT Token received." -ForegroundColor Yellow

# ------------------------------------------
# STEP 5: Create Monitor
# ------------------------------------------
$monitor = Call-API "5. Create Monitor" "POST" "$baseUrl/api/admin/monitors" -headers $authHeaders -body @{
    email    = "monitor_$rand@test.com"
    password = "password"
}
if (-not $monitor) { Write-Host "Monitor creation failed, stopping." -ForegroundColor Red; exit 1 }
$monitorId = $monitor.id
Write-Host "Monitor ID: $monitorId" -ForegroundColor Yellow

# ------------------------------------------
# STEP 6: Create Student
# ------------------------------------------
$student = Call-API "6. Create Student" "POST" "$baseUrl/api/admin/students" -headers $authHeaders -body @{
    email    = "student_$rand@test.com"
    password = "password"
    level    = "BEGINNER"
}
if (-not $student) { Write-Host "Student creation failed, stopping." -ForegroundColor Red; exit 1 }
$studentId = $student.id
Write-Host "Student ID: $studentId" -ForegroundColor Yellow

# ------------------------------------------
# STEP 7: Assign Monitor as main monitor for Student
# ------------------------------------------
Call-API "7. Link Student and Monitor" "PUT" "$baseUrl/api/admin/students/$studentId/main-monitor/$monitorId" -headers $authHeaders | Out-Null

# ------------------------------------------
# STEP 8: Get All Students
# ------------------------------------------
Call-API "8. Get All Students" "GET" "$baseUrl/api/admin/students" -headers $authHeaders | Out-Null

# ------------------------------------------
# STEP 9: Get All Monitors
# ------------------------------------------
Call-API "9. Get All Monitors" "GET" "$baseUrl/api/admin/monitors" -headers $authHeaders | Out-Null

# ------------------------------------------
# STEP 10: Create a Driving Session
# ------------------------------------------
$tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
$session = Call-API "10. Create Session" "POST" "$baseUrl/api/sessions" -headers $authHeaders -body @{
    monitorId   = $monitorId
    sessionDate = $tomorrow
    startTime   = "09:00:00"
    endTime     = "11:00:00"
    studentIds  = @($studentId)
}
$sessionId = if ($session) { $session.id } else { $null }
Write-Host "Session ID: $sessionId" -ForegroundColor Yellow

# ------------------------------------------
# STEP 11: Mark Session as Completed
# ------------------------------------------
if ($sessionId) {
    Call-API "11. Complete Session" "PUT" "$baseUrl/api/sessions/$sessionId/complete" -headers $authHeaders | Out-Null
}

# ------------------------------------------
# STEP 12: Register an Official Exam
# ------------------------------------------
$exam = Call-API "12. Register Official Exam" "POST" "$baseUrl/api/exams/register" -headers $authHeaders -body @{
    studentId = $studentId
    type      = "code"
    date      = $tomorrow
}
$examId = if ($exam) { $exam.id } else { $null }
Write-Host "Exam ID: $examId" -ForegroundColor Yellow

# ------------------------------------------
# STEP 13: Update Exam Result
# ------------------------------------------
if ($examId) {
    Call-API "13. Update Exam Result" "PUT" "$baseUrl/api/exams/$examId/result" -headers $authHeaders -body @{
        result = "success"
        score  = 85
    }
}

# ------------------------------------------
# STEP 14: Get Student Exams
# ------------------------------------------
Call-API "14. Get Exams for Student" "GET" "$baseUrl/api/exams/student/$studentId" -headers $authHeaders | Out-Null

Write-Host "`n==============================" -ForegroundColor Cyan
Write-Host "Full test flow completed." -ForegroundColor Green
