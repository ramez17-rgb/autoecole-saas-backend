$baseUrl = "http://localhost:8080"
$rand = Get-Random
$tenantEmail = "tenant_$rand@test.com"
$superAdminEmail = "superadmin@test.com"
$superAdminPassword = "password"

Write-Host "--- 1. Seed Super Admin ---" -ForegroundColor Cyan
try {
    $seedAdmin = Invoke-RestMethod -Uri "$baseUrl/api/auth/seed-super-admin" -Method Post
    Write-Host "Seed Result: $seedAdmin"
} catch {
    Write-Host "Super admin might already exist."
}

Write-Host "`n--- 2. Login as Super Admin ---" -ForegroundColor Cyan
$loginBody = @{ email = $superAdminEmail; password = $superAdminPassword } | ConvertTo-Json
$superToken = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$saHeaders = @{ Authorization = "Bearer $superToken" }
Write-Host "Logged in as Super Admin."

Write-Host "`n--- 3. Register New Tenant ---" -ForegroundColor Cyan
$regBody = @{
    name = "AutoEcole $rand"
    email = $tenantEmail
    address = "123 Test Street"
} | ConvertTo-Json

try {
    $regResponse = Invoke-RestMethod -Uri "$baseUrl/api/tenants/register" -Method Post -Body $regBody -ContentType "application/json"
    Write-Host "Registration Response: $($regResponse.message)"
    $tenantId = $regResponse.tenantId
} catch {
    Write-Error "Registration failed: $_"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Error "Response Body: $responseBody"
    }
    exit 1
}

Write-Host "`n--- 4. Verify Tenant is PENDING ---" -ForegroundColor Cyan
$tenants = Invoke-RestMethod -Uri "$baseUrl/api/super-admin/tenants" -Method Get -Headers $saHeaders
$myTenant = $tenants | Where-Object { $_.id -eq $tenantId }
Write-Host "Tenant Status: $($myTenant.status)"
if ($myTenant.status -ne "PENDING") { Write-Error "Status should be PENDING!"; exit 1 }

Write-Host "`n--- 5. Approve Tenant ---" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$baseUrl/api/super-admin/tenants/$tenantId/approve" -Method Post -Headers $saHeaders
Write-Host "Tenant Approved."

Write-Host "`n--- 6. Get Setup Token (via Dev Helper) ---" -ForegroundColor Cyan
# In real life this arrives via email. We use a dev helper for testing.
$setupToken = Invoke-RestMethod -Uri "$baseUrl/api/dev/last-token?email=$tenantEmail" -Method Get
Write-Host "Setup Token: $setupToken"

Write-Host "`n--- 7. Setup Password (with Email Verification) ---" -ForegroundColor Cyan
$setupBody = @{
    email = $tenantEmail
    password = "newpassword123"
} | ConvertTo-Json
$setupResult = Invoke-RestMethod -Uri "$baseUrl/api/auth/setup-password?token=$setupToken" -Method Post -Body $setupBody -ContentType "application/json"
Write-Host "Setup Result: $setupResult"

Write-Host "`n--- 8. Login as New Tenant Admin ---" -ForegroundColor Cyan
$tenantLoginBody = @{ email = $tenantEmail; password = "newpassword123" } | ConvertTo-Json
$tenantToken = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $tenantLoginBody -ContentType "application/json"
Write-Host "Login Successful. Received JWT."

Write-Host "`n--- 9. Test Resend Link ---" -ForegroundColor Cyan
$resendBody = @{ email = $tenantEmail } | ConvertTo-Json
try {
    $resendResult = Invoke-RestMethod -Uri "$baseUrl/api/auth/resend-setup-link" -Method Post -Body $resendBody -ContentType "application/json"
    Write-Host "Resend Result: $resendResult"
} catch {
    Write-Error "Resend failed: $_"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Error "Response Body: $responseBody"
    }
}

Write-Host "`n--- VERIFICATION SUCCESSFUL ---" -ForegroundColor Green
