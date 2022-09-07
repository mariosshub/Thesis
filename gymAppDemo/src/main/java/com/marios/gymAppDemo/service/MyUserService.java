package com.marios.gymAppDemo.service;

import com.marios.gymAppDemo.repository.LoginDetailsRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class MyUserService implements UserDetailsService {
    private final LoginDetailsRepository loginDetailsRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loginDetailsRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user with username " + username));
    }
}
