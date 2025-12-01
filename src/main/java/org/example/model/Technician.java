package org.example.model;

public class Technician {
    private final String username;
    private final String password;

    public Technician(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}


