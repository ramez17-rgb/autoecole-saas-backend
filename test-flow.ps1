$ErrorActionPreference = "Stop"

Write-Host "1. Registering independent student..."
$registerPayload = @{
    email = "teststudent$(Get-Random)@example.com"
    password = "password123"
} | ConvertTo-Json

$studentToken = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/student/register" -Method Post -Body $registerPayload -ContentType "application/json"
Write-Host "Student Token Acquired."

Write-Host "2. Getting public schools..."
$schools = Invoke-RestMethod -Uri "http://localhost:8080/api/public/schools" -Method Get -Headers @{ Authorization = "Bearer $studentToken" }
if ($schools.Count -eq 0) {
    Write-Host "No active schools found to apply to. Please ensure a school is active."
    exit
}

$schoolId = $schools[0].id
Write-Host "Selected School ID: $schoolId"

Write-Host "3. Applying to school..."
$enrollment = Invoke-RestMethod -Uri "http://localhost:8080/api/enrollments/schools/$schoolId" -Method Post -Headers @{ Authorization = "Bearer $studentToken" }
Write-Host "Applied successfully. Request ID: $($enrollment.id), Status: $($enrollment.status)"

Write-Host "================= All Hybrid Flow steps tested successfully up to admin review! ================="
