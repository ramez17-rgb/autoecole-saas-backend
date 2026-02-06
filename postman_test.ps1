$baseUrl = "http://localhost:8080"
$adminEmail = "admin@test.com"
$adminPassword = "password"

function Log-Request {
    param (
        [string]$Method,
        [string]$Uri,
        [string]$Body
    )
    Write-Host "----------------------------------------------------------------" -ForegroundColor Cyan
    Write-Host "$Method $Uri" -ForegroundColor Green
    if ($Body) {
        Write-Host "Body:" -ForegroundColor Yellow
        Write-Host $Body
    }
}

function Log-Response {
    param (
        [string]$StatusCode,
        [object]$Body
    )
    Write-Host "Status: $StatusCode" -ForegroundColor Magenta
    Write-Host "Response:" -ForegroundColor White
    Write-Host ($Body | ConvertTo-Json -Depth 5)
    Write-Host "----------------------------------------------------------------`n" -ForegroundColor Cyan
}

# 1. Login (GET TOKEN)
$loginUri = "$baseUrl/api/auth/login"
$loginBody = @{
    email = "admin@test.com"
    password = "password"
} | ConvertTo-Json

Log-Request "POST" $loginUri $loginBody

try {
    $token = Invoke-RestMethod -Uri $loginUri -Method Post -Body $loginBody -ContentType "application/json"
    Log-Response "200 OK" $token
    
    $headers = @{
        Authorization = "Bearer $token"
    }
} catch {
    Write-Error "Login failed: $_"
    exit 1
}

# 2. Create Student
$rand = Get-Random
$studentUri = "$baseUrl/api/admin/students"
$studentBody = @{
    email = "student_postman_$rand@test.com"
    password = "password"
    level = "BEGINNER"
} | ConvertTo-Json

Log-Request "POST" $studentUri $studentBody

try {
    $studentParam = @{
        Uri         = $studentUri
        Method      = "Post"
        Body        = $studentBody
        ContentType = "application/json"
        Headers     = $headers
    }
    $student = Invoke-RestMethod @studentParam
    Log-Response "200 OK" $student
} catch {
     if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Log-Response $_.Exception.Response.StatusCode $responseBody
    } else {
        Write-Error "Student creation failed: $_"
    }
}

# 3. Create Monitor
$rand2 = Get-Random
$monitorUri = "$baseUrl/api/admin/monitors"
$monitorBody = @{
    email = "monitor_postman_$rand2@test.com"
    password = "password"
} | ConvertTo-Json

Log-Request "POST" $monitorUri $monitorBody

try {
     $monitorParam = @{
        Uri         = $monitorUri
        Method      = "Post"
        Body        = $monitorBody
        ContentType = "application/json"
        Headers     = $headers
    }
    $monitor = Invoke-RestMethod @monitorParam
    Log-Response "200 OK" $monitor
} catch {
     if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
         Log-Response $_.Exception.Response.StatusCode $responseBody
    } else {
        Write-Error "Monitor creation failed: $_"
    }
}
