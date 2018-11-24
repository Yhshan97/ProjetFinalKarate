package com.example.luxed.karatefrontend.Entities;

public class Account {
    private String email;
    private String image;

    public Account(String email, String image) {
        this.email = email;
        this.image = image;
    }

    public Account() {
        this.email = "";
        this.image = "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
