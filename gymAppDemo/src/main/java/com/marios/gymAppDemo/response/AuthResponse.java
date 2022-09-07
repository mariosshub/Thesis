package com.marios.gymAppDemo.response;

import com.marios.gymAppDemo.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String authenticationToken;
    private String username;
    private Role role;
    private String refreshToken;
    private Instant expiresAt;

}
