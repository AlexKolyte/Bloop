package com.luv2code.springboot.demo.entity;

public class UserDescription {
    private String name;
    private String description;


    public UserDescription(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getUsername() {
        return name;
    }

    public void setUsername(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
