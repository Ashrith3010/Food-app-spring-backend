package com.food_api.food_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "account_settings")
public class AccountSettings {
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private Boolean notifications = true;

    @Column(nullable = false)
    private Boolean emailUpdates = true;

    @Column(nullable = false)
    private Boolean privacyMode = false;

    // Default constructor
    public AccountSettings() {}

    // Parameterized constructor
    public AccountSettings(Long id, User user, Boolean notifications, Boolean emailUpdates, Boolean privacyMode) {
        this.id = id;
        this.user = user;
        this.notifications = notifications != null ? notifications : true;
        this.emailUpdates = emailUpdates != null ? emailUpdates : true;
        this.privacyMode = privacyMode != null ? privacyMode : false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getNotifications() {
        return notifications;
    }

    public void setNotifications(Boolean notifications) {
        this.notifications = notifications != null ? notifications : true;
    }

    public Boolean getEmailUpdates() {
        return emailUpdates;
    }

    public void setEmailUpdates(Boolean emailUpdates) {
        this.emailUpdates = emailUpdates != null ? emailUpdates : true;
    }

    public Boolean getPrivacyMode() {
        return privacyMode;
    }

    public void setPrivacyMode(Boolean privacyMode) {
        this.privacyMode = privacyMode != null ? privacyMode : false;
    }
}
