# Driving School SaaS - Postman Testing Guide

This guide walks you through the complete flow of the application, from registering a new school (tenant) to managing students, monitors, and driving sessions.

## 🚀 Getting Started

1.  **Base URL**: `http://localhost:8080/api`
2.  **Postman Environment**: Create an environment and add a variable `baseUrl` with the value above.
3.  **Authentication**: Most admin endpoints require a JWT token.

---

## 1. Tenant Lifecycle (Registration & Approval)

### Step 1: Register a New School (Public)
**Method**: `POST`
**URL**: `{{baseUrl}}/tenants/register`
**Body (JSON)**:
```json
{
  "name": "Eagle Driving School",
  "email": "eagle@admin.com",
  "address": "123 Sky Way"
}
```
*Note: This status will be `PENDING` initially.*

### Step 2: Super Admin Login
You need to be a Super Admin to approve the school.
**Method**: `POST`
**URL**: `{{baseUrl}}/auth/login`
**Body (JSON)**:
```json
{
  "email": "superadmin@example.com",
  "password": "password"
}
```
*Note: Save the returned JWT token to your `token` variable.*

### Step 3: Approve the Tenant
**Method**: `POST`
**URL**: `{{baseUrl}}/super-admin/tenants/{tenantId}/approve`
**Headers**: `Authorization: Bearer {{token}}`
*Note: The response will now contain a `setupToken`. Use this for the next step.*

---

## 2. Operations (Admin Flow)

Once approved, the school admin can log in and manage the school.

### Step 4: Create a Monitor
**Method**: `POST`
**URL**: `{{baseUrl}}/admin/monitors`
**Headers**: `Authorization: Bearer {{token}}`
**Body (JSON)**:
```json
{
  "email": "monitor1@eagle.com",
  "password": "password123"
}
```

### Step 5: Create a Student
**Method**: `POST`
**URL**: `{{baseUrl}}/admin/students`
**Headers**: `Authorization: Bearer {{token}}`
**Body (JSON)**:
```json
{
  "email": "student1@gmail.com",
  "password": "password123",
  "level": "BEGINNER"
}
```

### Step 6: Link Student to Main Monitor
**Method**: `PUT`
**URL**: `{{baseUrl}}/admin/students/{studentId}/main-monitor/{monitorId}`
**Headers**: `Authorization: Bearer {{token}}`

---

## 3. Session Management

### Step 7: Create a Driving Session
**Method**: `POST`
**URL**: `{{baseUrl}}/sessions`
**Headers**: `Authorization: Bearer {{token}}`
**Body (JSON)**:
```json
{
  "monitorId": "{monitorId}",
  "sessionDate": "2026-04-01",
  "startTime": "09:00:00",
  "endTime": "10:00:00",
  "studentIds": ["{studentId}"]
}
```

---

## 💡 Pro Tips
- Use the **Tests** tab in Postman to automatically save IDs from responses:
  ```javascript
  pm.environment.set("tenantId", pm.response.json().tenantId);
  ```
