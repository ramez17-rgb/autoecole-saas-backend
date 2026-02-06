package com.autoecole.saas.student.dto;

public class CreateStudentRequest {

    private String email;
    private String password;
    private String level;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getLevel() {
        return level;
    }
}
