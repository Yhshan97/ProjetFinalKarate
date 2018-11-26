package com.example.luxed.karatefrontend.Entities;

public class Account {
    private String sessionId;

    private String email;
    private String fullName;
    private String avatar;
    private String role;
    private String groupe;

    public Account(String email, String fullName, String avatar, String role, String groupe) {
        this.email = email;
        this.fullName = fullName;
        this.avatar = avatar;
        this.role = role;
        this.groupe = groupe;
    }

    public Account() {
        this.email = "";
        this.avatar = "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String  getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getGroupe() {
        return groupe;
    }
}
