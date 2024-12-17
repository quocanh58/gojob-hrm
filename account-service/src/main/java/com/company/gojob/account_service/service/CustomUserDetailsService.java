package com.company.gojob.account_service.service;

import com.company.gojob.account_service.model.CustomerDetail;
import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<UserCredential> userCredential = repository.findByUsername(username);
        return userCredential.map(CustomerDetail::new).orElseThrow(()->new UsernameNotFoundException("User not found: " + username));
    }
}
