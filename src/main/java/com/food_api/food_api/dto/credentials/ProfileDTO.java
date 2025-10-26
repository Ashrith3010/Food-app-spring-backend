package com.food_api.food_api.dto.credentials;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.food_api.food_api.entity.User;
import com.food_api.food_api.entity.AccountSettings;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String type;
    private String organization;
    private String area;
    private Boolean emailUpdates;

    // Create a simplified settings object that doesn't include the user reference
    @JsonIgnoreProperties("user")  // This prevents the User object from being serialized within settings
    private AccountSettings settings;

    // Default constructor
    public ProfileDTO() {
    }

    // Constructor from User entity
    public ProfileDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.type = user.getType();
        this.organization = user.getOrganization();
        this.area = user.getArea();
        this.emailUpdates = user.getEmailUpdates();
        this.settings = user.getSettings();  // The @JsonIgnoreProperties annotation will prevent infinite recursion
    }

    // Getters and setters remain the same
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Boolean getEmailUpdates() {
        return emailUpdates;
    }

    public void setEmailUpdates(Boolean emailUpdates) {
        this.emailUpdates = emailUpdates;
    }

    public AccountSettings getSettings() {
        return settings;
    }

    public void setSettings(AccountSettings settings) {
        this.settings = settings;
    }
}