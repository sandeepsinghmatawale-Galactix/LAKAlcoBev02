package com.barinventory.auth.servcies;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.barinventory.auth.entities.BarUser;
import com.barinventory.auth.repos.BarUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final BarUserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) {

        BarUser user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new CustomUserDetails(user);
    }
}