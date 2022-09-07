package com.marios.gymAppDemo.security;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String bearerToken = request.getHeader("Authorization");

        if(bearerToken !=null){
            String jwt = null;
            String username = null;
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                jwt = bearerToken.substring(7);
                username = jwtProvider.getUsernameFromJwt(jwt);
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (StringUtils.hasText(username) && authentication == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                //check if token is valid and account not expired (user has logged out)
                if (jwtProvider.validateToken(jwt, userDetails) && userDetails.isAccountNonExpired()) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
