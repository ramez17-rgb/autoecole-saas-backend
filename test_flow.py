import requests
import uuid
import datetime
import random

BASE_URL = "http://localhost:8080"
headers_superadmin = {}
headers_admin = {}

def print_step(step_name):
    print(f"\n--- {step_name} ---")

def run():
    # 1. Seed Super Admin
    print_step("Seed Super Admin")
    res = requests.post(f"{BASE_URL}/api/auth/seed-super-admin")
    print("Seed Super Admin:", res.text)

    # 2. Login Super Admin
    print_step("Login Super Admin")
    res = requests.post(f"{BASE_URL}/api/auth/login", json={"email": "superadmin@test.com", "password": "password"})
    if res.status_code == 200:
        headers_superadmin["Authorization"] = f"Bearer {res.text}"
        print("Super Admin Login OK")
    else:
        print("Super Admin Login Failed", res.text)

    # 3. Create Tenant (Using Seed endpoint)
    print_step("Create Tenant & Admin Test Data")
    res = requests.post(f"{BASE_URL}/api/auth/seed")
    print("Seed Tenant:", res.text)

    # 4. Login Admin
    print_step("Login Admin")
    res = requests.post(f"{BASE_URL}/api/auth/login", json={"email": "admin@test.com", "password": "password"})
    if res.status_code == 200:
        headers_admin["Authorization"] = f"Bearer {res.text}"
        print("Admin Login OK")
    else:
        print("Admin Login Failed", res.text)
        return

    # 5. Create Monitor
    rand = random.randint(1000, 9999)
    print_step("Create Monitor")
    monitor_email = f"monitor_{rand}@test.com"
    res = requests.post(f"{BASE_URL}/api/admin/monitors", headers=headers_admin, json={"email": monitor_email, "password": "password"})
    monitor_id = None
    if res.status_code == 200:
        monitor_id = res.json()["id"]
        print(f"Monitor created: {monitor_id}")
    else:
        print("Failed to create monitor", res.text)
        return

    # 6. Create Student
    print_step("Create Student")
    student_email = f"student_{rand}@test.com"
    res = requests.post(f"{BASE_URL}/api/admin/students", headers=headers_admin, json={"email": student_email, "password": "password", "level": "BEGINNER"})
    student_id = None
    if res.status_code == 200:
        student_id = res.json()["id"]
        print(f"Student created: {student_id}")
    else:
        print("Failed to create student", res.text)
        return

    # 7. Link Student & Monitor
    print_step("Link Student and Monitor")
    res = requests.put(f"{BASE_URL}/api/admin/students/{student_id}/main-monitor/{monitor_id}", headers=headers_admin)
    if res.status_code == 200:
        print("Student linked to Monitor successfully.")
    else:
        print("Failed to link student and monitor", res.text)

    # 8. Create Session
    print_step("Create Session")
    tomorrow = (datetime.date.today() + datetime.timedelta(days=1)).isoformat()
    session_data = {
        "monitorId": monitor_id,
        "sessionDate": tomorrow,
        "startTime": "09:00:00",
        "endTime": "11:00:00",
        "studentIds": [student_id]
    }
    res = requests.post(f"{BASE_URL}/api/sessions", headers=headers_admin, json=session_data)
    session_id = None
    if res.status_code == 201:
        session_id = res.json()["id"]
        print(f"Session created: {session_id}")
    else:
        print("Failed to create session", res.text)

    # 9. Update/Complete Session
    if session_id:
        print_step("Complete Session")
        res = requests.put(f"{BASE_URL}/api/sessions/{session_id}/complete", headers=headers_admin)
        if res.status_code == 200:
            print("Session marked as completed.")
        else:
            print("Failed to complete session", res.text)

    # 10. Test Official Exam
    print_step("Register Official Exam")
    exam_data = {
        "studentId": student_id,
        "type": "code",
        "date": tomorrow
    }
    res = requests.post(f"{BASE_URL}/api/exams/register", headers=headers_admin, json=exam_data)
    exam_id = None
    if res.status_code == 201:
        exam_id = res.json()["id"]
        print(f"Exam registered: {exam_id}")
    else:
        print("Failed to register exam", res.text)
    
    if exam_id:
        print_step("Update Exam Result")
        res = requests.put(f"{BASE_URL}/api/exams/{exam_id}/result", headers=headers_admin, json={"score": 85, "passed": True})
        if res.status_code == 200:
            print("Exam result updated.")
        else:
            print("Failed to update exam result", res.text)
            
if __name__ == '__main__':
    run()
