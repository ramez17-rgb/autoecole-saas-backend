$baseUrl = "http://localhost:8080"

# 1. Seed Admin
Write-Host "Seeding Admin..."
try {
    $seedResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/seed" -Method Post
    Write-Host "Seed Response: $seedResponse"
} catch {
    Write-Host "Seed step failed or user already exists: $_"
}

# 2. Login
Write-Host "Logging in..."
$loginBody = @{
    email = "admin@test.com"
    password = "password"
} | ConvertTo-Json

try {
    $token = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    Write-Host "Token received."
    $headers = @{
        Authorization = "Bearer $token"
    }
} catch {
    Write-Error "Login failed: $_"
    exit 1
}

# 3. Create Student
Write-Host "Creating Student..."
$rand = Get-Random
$studentBody = @{
    email = "student_test_$rand@test.com"
    password = "password"
    level = "BEGINNER"
} | ConvertTo-Json

try {
    $studentParam = @{
        Uri         = "$baseUrl/api/admin/students"
        Method      = "Post"
        Body        = $studentBody
        ContentType = "application/json"
        Headers     = $headers
    }
    $student = Invoke-RestMethod @studentParam
    Write-Host "Student Created: ID=$($student.id)"
} catch {
    Write-Error "Student creation failed: $_"
     if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Error "Response Body: $responseBody"
    }
}

# 4. Create Monitor
Write-Host "Creating Monitor..."
$monitorBody = @{
    email = "monitor_test_$rand@test.com"
    password = "password"
} | ConvertTo-Json

try {
     $monitorParam = @{
        Uri         = "$baseUrl/api/admin/monitors"
        Method      = "Post"
        Body        = $monitorBody
        ContentType = "application/json"
        Headers     = $headers
    }
    $monitor = Invoke-RestMethod @monitorParam
    Write-Host "Monitor Created: ID=$($monitor.id)"
} catch {
    Write-Error "Monitor creation failed: $_"
     if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Error "Response Body: $responseBody"
    }
}
