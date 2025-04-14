package com.example.demo.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    
    public JwtResponse(String accessToken, Long id, String username) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
    }
}
