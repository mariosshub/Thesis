package com.marios.gymAppDemo.service;

import com.marios.gymAppDemo.request.RefreshTokenRequest;
import com.marios.gymAppDemo.customException.CustomNotFoundException;
import com.marios.gymAppDemo.model.LoginDetails;
import com.marios.gymAppDemo.model.RefreshToken;
import com.marios.gymAppDemo.repository.LoginDetailsRepository;
import com.marios.gymAppDemo.repository.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository tokenRepository;
    private final LoginDetailsRepository loginDetailsRepository;

    // generate a refresh token
    public RefreshToken generateRefreshToken() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setCreatedDate(Instant.now());

        return tokenRepository.save(refreshToken);
    }

    // find if the refresh token exists
    void validateRefreshToken(String token) {
        tokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomNotFoundException("invalid_token"));
    }

    // implements log out
    // delete refresh token
    public void deleteRefreshToken(RefreshTokenRequest request) {
        tokenRepository.deleteByToken(request.getRefreshToken());
        LoginDetails user = loginDetailsRepository.findByUsername(request.getUsername()).orElseThrow(() ->new CustomNotFoundException("user_not_found"));
        //set to false and when user logs in set to true
        user.setNotExpired(false);
        loginDetailsRepository.save(user);
    }
}
