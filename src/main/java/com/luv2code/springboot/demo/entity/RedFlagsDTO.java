package com.luv2code.springboot.demo.entity;


import java.util.ArrayList;
import java.util.List;

public class RedFlagsDTO {
    private String username;
    private List<String> flags = new ArrayList<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getFlags() {
        return flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }
}
