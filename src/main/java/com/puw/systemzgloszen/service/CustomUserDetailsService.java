package com.puw.systemzgloszen.service;

import com.puw.systemzgloszen.entity.AppUser;
import com.puw.systemzgloszen.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = appUserRepository.findByUsername(username);
        if (user == null) throw new UsernameNotFoundException("Nie znaleziono użytkownika: " + username);

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().toString())
                .build();
    }
}

