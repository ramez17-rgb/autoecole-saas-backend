package com.autoecole.saas.student.dto;

public class CreateStudentRequest {

    private String email;
    private String password;
    private String level;
    private String nom;
    private String prenom;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getLevel() {
        return level;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }
}
