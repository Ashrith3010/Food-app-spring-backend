package com.food_api.food_api.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountSettingsDTO {
    private Boolean notifications;
    private Boolean emailUpdates;
    private Boolean privacyMode;

    public Boolean getNotifications() {
        return notifications;
    }

    public void setNotifications(Boolean notifications) {
        this.notifications = notifications;
    }

    public Boolean getEmailUpdates() {
        return emailUpdates;
    }

    public void setEmailUpdates(Boolean emailUpdates) {
        this.emailUpdates = emailUpdates;
    }

    public Boolean getPrivacyMode() {
        return privacyMode;
    }

    public void setPrivacyMode(Boolean privacyMode) {
        this.privacyMode = privacyMode;
    }
}