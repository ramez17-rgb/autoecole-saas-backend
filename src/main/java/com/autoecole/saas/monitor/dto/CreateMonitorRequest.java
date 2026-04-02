package com.autoecole.saas.monitor.dto;

public class CreateMonitorRequest {

    private String email;
    private String password;
    private String nom;
    private String prenom;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }
}
