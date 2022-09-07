package com.marios.gymAppDemo.service;

import com.marios.gymAppDemo.request.*;
import com.marios.gymAppDemo.response.AuthResponse;
import com.marios.gymAppDemo.security.JwtProvider;
import com.marios.gymAppDemo.customException.CustomNotFoundException;
import com.marios.gymAppDemo.model.Customer;
import com.marios.gymAppDemo.model.LoginDetails;
import com.marios.gymAppDemo.model.Role;
import com.marios.gymAppDemo.repository.CustomerRepository;
import com.marios.gymAppDemo.repository.LoginDetailsRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginDetailsRepository loginDetailsRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public Customer findCustomerByUsername(String username){
        return customerRepository.findCustomerByUsername(username).orElseThrow(() -> new CustomNotFoundException("user_not_found"));
    }

    // updates customer's details and returns the updated Customer object
    public Customer updateCustomer(UpdateProfileRequest profileRequest, String username){
        Customer updatedCustomer = customerRepository.findCustomerByUsername(username).
                orElseThrow(() -> new CustomNotFoundException("user_not_found"));
        updatedCustomer.setName(profileRequest.getName());
        updatedCustomer.setSurname(profileRequest.getSurname());
        updatedCustomer.setEmail(profileRequest.getEmail());
        updatedCustomer.setPhone(profileRequest.getPhone());
        return customerRepository.save(updatedCustomer);
    }

    //sets the imgUrl field of the Customer object
    public Boolean updatePhoto(String imgUrl, String username){
        Customer updatedCustomer = customerRepository.findCustomerByUsername(username).
                orElseThrow(() -> new CustomNotFoundException("user_not_found"));

        updatedCustomer.setImgUrl(imgUrl);
        customerRepository.save(updatedCustomer);
        return true;
    }

    // registers a new Customer
    public Customer registerCustomer(SignUpRequest request){
        Optional<Customer> existingCustomer = customerRepository.findCustomerByUsername(request.getUsername());
        Optional<Customer> existingMail = customerRepository.findCustomerByEmail(request.getEmail());
        //check if Customer exists by username or email
        if(existingCustomer.isEmpty()){
            if(existingMail.isEmpty()){
                Customer customer = new Customer();
                customer.setName(request.getName());
                customer.setSurname(request.getSurname());
                customer.setPhone(request.getPhone());
                customer.setEmail(request.getEmail());
                customer.setUsername(request.getUsername());
                customer.setImgUrl(request.getImgUrl());
                customer.setRole(Role.CUSTOMER);
                String encryptedPass = passwordEncoder.encode(request.getPassword());
                customer.setPassword(encryptedPass);
                customer.setNotExpired(true);
                return customerRepository.save(customer);
            }
            else{
                throw new CustomNotFoundException("email_exists");
            }
        }
        else{
            throw new CustomNotFoundException("user_exists");
        }
    }

    // login customer using jwt token and return AuthResponse object if successful
    public AuthResponse loginJWT(LoginRequest request) {
        LoginDetails user = loginDetailsRepository.findByUsername(request.getUsername()).orElseThrow(()->new CustomNotFoundException("user_not_found"));
       //check if user's account is expired (when logged out this boolean is set to false)
        if(!user.isAccountNonExpired())
            user.setNotExpired(true);
        loginDetailsRepository.save(user);
        try {
            Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authenticate);
            String token = jwtProvider.generateToken(authenticate);
            return AuthResponse.builder()
                    .authenticationToken(token)
                    .refreshToken(refreshTokenService.generateRefreshToken().getToken())
                    .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                    .username(request.getUsername())
                    .role(user.getRole())
                    .build();
        }
        catch (AuthenticationException exception){
            throw new CustomNotFoundException("bad_creds");
        }
    }

    // generate a new JWT token and return it with AuthResponse
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        LoginDetails user = loginDetailsRepository.findByUsername(request.getUsername()).orElseThrow(()->new CustomNotFoundException("user_not_found"));
        refreshTokenService.validateRefreshToken(request.getRefreshToken());
        //generate a new jwt token
        String token = jwtProvider.generateTokenWithUserName(request.getUsername());
            return AuthResponse.builder()
                .authenticationToken(token)
                .refreshToken(request.getRefreshToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .username(request.getUsername())
                .role(user.getRole())
                .build();
    }

}
