package com.example.tuition_system.service;

import com.example.tuition_system.model.User;
import com.example.tuition_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));

                System.out.println(">>> [DEBUG] Found user: " + u.getUsername());
                System.out.println(">>> [DEBUG] Role: " + u.getRole());
                System.out.println(">>> [DEBUG] Password hash: " + u.getPassword());
                // ⚙️ KHÔNG thêm ROLE_ nữa vì DB đã có sẵn
                List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority(u.getRole().toUpperCase())
                );

        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getUsername())
                .password(u.getPassword())  // BCrypt hash
                .authorities(authorities)
                .build();
    }
}
