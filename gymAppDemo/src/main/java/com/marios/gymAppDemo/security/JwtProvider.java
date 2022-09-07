package com.marios.gymAppDemo.security;

import com.marios.gymAppDemo.customException.CustomNotFoundException;
import com.marios.gymAppDemo.model.LoginDetails;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.Date;
import static io.jsonwebtoken.Jwts.parserBuilder;

@Service
public class JwtProvider {
    private KeyStore keyStore;

    @Value("${jwt.expiration.time}")
    private Long jwtExpirationInMillis;

    @PostConstruct
    public void init() {
        try {
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourceAsStream = getClass().getResourceAsStream("/springsecure.jks");
            keyStore.load(resourceAsStream, "YOURPASS".toCharArray());
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new CustomNotFoundException("Exception occurred while loading keystore");
        }

    }

    public String generateToken(Authentication authentication) {
        LoginDetails principal = (LoginDetails) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuedAt(Date.from(Instant.now()))
                .signWith(getPrivateKey())
                .setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationInMillis)))
                .compact();
    }

    public String generateTokenWithUserName(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(Instant.now()))
                .signWith(getPrivateKey())
                .setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationInMillis)))
                .compact();
    }

    public Boolean validateToken(String jwt, UserDetails userDetails) {
        String username = getUsernameFromJwt(jwt);
        return (username.equals(userDetails.getUsername()));
    }

    public String getUsernameFromJwt(String token) {
        return parserBuilder()
                .setSigningKey(getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) keyStore.getKey("springsecure", "YOURPASS".toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new CustomNotFoundException("Exception occurred while retrieving public key from keystore");
        }
    }

    private PublicKey getPublicKey() {
        try {
            return keyStore.getCertificate("springsecure").getPublicKey();
        } catch (KeyStoreException e) {
            throw new CustomNotFoundException("Exception occurred while retrieving public key from keystore");
        }
    }

    public Long getJwtExpirationInMillis() {
        return jwtExpirationInMillis;
    }
}
