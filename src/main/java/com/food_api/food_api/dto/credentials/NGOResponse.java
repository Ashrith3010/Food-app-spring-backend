package com.food_api.food_api.dto.credentials;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NGOResponse {
    private Long id;
    private String username;
    private String organization;
    private String area;
    private String phone;
    private String email;
    private LocalDateTime createdAt;

    // Constructor with all fields
    public NGOResponse(Long id, String username, String organization,
                       String area, String phone, String email,
                       LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.organization = organization;
        this.area = area;
        this.phone = phone;
        this.email = email;
        this.createdAt = createdAt;
    }
}
