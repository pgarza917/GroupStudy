package com.example.studygroup.models;

/*
    Class for Firebase User
*/
public class User {

    private String id;
    private String username;
    private String imageUrl;
    private String email;

    public User() {}

    public User(String id, String username, String imageUrl, String email) {
        this.id = id;
        this.username = username;
        this.imageUrl = imageUrl;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
