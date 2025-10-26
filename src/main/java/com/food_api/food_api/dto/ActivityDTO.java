package com.food_api.food_api.dto;
import java.time.LocalDateTime;

public class ActivityDTO {
    private String type;
    private String message;
    private LocalDateTime timestamp;
    private String actorName;
    private String actorType;

    public ActivityDTO(String type, String message, LocalDateTime timestamp, String actorName, String actorType) {
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.actorName = actorName;
        this.actorType = actorType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorType() {
        return actorType;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

// Getters and setters
    // Constructor omitted for brevity
}
