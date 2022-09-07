package com.marios.gymAppDemo.controller;

import com.marios.gymAppDemo.request.LoginRequest;
import com.marios.gymAppDemo.request.RefreshTokenRequest;
import com.marios.gymAppDemo.request.SignUpRequest;
import com.marios.gymAppDemo.response.AuthResponse;
import com.marios.gymAppDemo.model.Customer;
import com.marios.gymAppDemo.service.CustomerService;
import com.marios.gymAppDemo.service.RefreshTokenService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@AllArgsConstructor
public class AuthController {

    private final CustomerService customerService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("signup")
    public ResponseEntity<Customer> registerUser(@RequestBody SignUpRequest request) {
        Customer newCustomer = customerService.registerCustomer(request);
        return new ResponseEntity<>(newCustomer,HttpStatus.OK);
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse authResponse = customerService.loginJWT(request);
        return new ResponseEntity<>(authResponse,HttpStatus.OK);
    }

    // refreshes the jwt token
    @PostMapping("refresh/token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = customerService.refreshToken(request);
        return new ResponseEntity<>(authResponse,HttpStatus.OK);
    }

    @PostMapping("logoutCustomer")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.deleteRefreshToken(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
