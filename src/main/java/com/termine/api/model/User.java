package com.termine.api.model;

import java.util.List;

public class User {
    private Long id;
    private String name;
    private List<String> roles;
    
    public User() {
    }

    public User(Long id, String name, List<String> roles) {
        this.id = id;
        this.name = name;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

}