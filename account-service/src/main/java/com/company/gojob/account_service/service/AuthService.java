package com.company.gojob.account_service.service;

import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.repository.AuthRepository;
import com.company.gojob.account_service.service.implement.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {
    @Autowired
    AuthRepository authRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtService jwtService;

    @Override
    public String createUser(UserCredential userCredential) {
        userCredential.setPassword(passwordEncoder.encode(userCredential.getPassword()));
        authRepository.save(userCredential);
        return "Create user successfully.";
    }

    public String generateToken(String userName){
        return jwtService.generateToken(userName);
    }

    public void invalidateToken(String token){
        jwtService.validateToken(token);
    }


}
