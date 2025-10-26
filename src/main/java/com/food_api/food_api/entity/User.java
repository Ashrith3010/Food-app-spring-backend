package com.food_api.food_api.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String organization;

    private String area;

    @Column(name = "email_updates", nullable = false)
    private Boolean emailUpdates = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private AccountSettings settings;

    // Default constructor
    public User() {}

    // Parameterized constructor
    public User(Long id, String username, String password, String email, String phone, String type,
                LocalDateTime createdAt, String organization, String area, Boolean emailUpdates, AccountSettings settings) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.type = type;
        this.createdAt = createdAt;
        this.organization = organization;
        this.area = area;
        this.emailUpdates = emailUpdates != null ? emailUpdates : true;
        this.settings = settings;
        if (settings != null) {
            settings.setUser(this);
        }
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
        this.emailUpdates = emailUpdates != null ? emailUpdates : true;
    }

    public AccountSettings getSettings() {
        return settings;
    }

    public void setSettings(AccountSettings settings) {
        if (settings != null) {
            settings.setUser(this); // Ensure the link is properly set
        }
        this.settings = settings;
    }

    // Implementation of UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.type.toUpperCase()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
