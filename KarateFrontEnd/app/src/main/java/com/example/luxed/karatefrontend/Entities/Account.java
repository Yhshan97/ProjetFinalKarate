package com.example.luxed.karatefrontend.Entities;

public class Account/* implements Comparable*/ {
    private String sessionId;

    private String email;
    private String fullName;
    private String avatar;
    private String role;
    private String groupe;
    private int points;
    private int credits;

    public Account(String email, String fullName, String avatar, String role, String groupe,
                   int points, int credits) {
        this.email = email;
        this.fullName = fullName;
        this.avatar = avatar;
        this.role = role;
        this.groupe = groupe;
        this.points = points;
        this.credits = credits;
    }

    public Account() {
        this.sessionId = "";
        this.email = "";
        this.avatar = "";
    }

    /*@Override
    public boolean equals(Object acc) {
        return email.equals(((Account) acc).getEmail());
    }

    @Override
    public int compareTo(Object acc) {
        Account account = (Account) acc;
    }*/

    public String getEmail() {
        return email;
    }

    public String getAvatar() {
        return avatar;
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

    public int getPoints() { return points; }

    public int getCredits() { return credits; }
}
