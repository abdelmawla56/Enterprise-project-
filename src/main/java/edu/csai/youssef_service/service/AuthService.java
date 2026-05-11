package edu.csai.youssef_service.service;

import edu.csai.youssef_service.dto.AuthResponse;
import edu.csai.youssef_service.dto.LoginRequest;
import edu.csai.youssef_service.dto.UserResponse;
import edu.csai.youssef_service.entity.User;
import edu.csai.youssef_service.repository.UserRepository;
import edu.csai.youssef_service.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private JwtUtils jwtUtils;

        @Autowired
        private AuthenticationManager authenticationManager;

        public AuthResponse login(LoginRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                String token = jwtUtils.generateToken(user.getEmail(), user.getTenantId(), user.getRoles());

                return AuthResponse.builder()
                                .token(token)
                                .tenantId(user.getTenantId())
                                .build();
        }

        public UserResponse getCurrentUser() {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                return UserResponse.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .roles(user.getRoles())
                                .tenantId(user.getTenantId())
                                .build();
        }
}
