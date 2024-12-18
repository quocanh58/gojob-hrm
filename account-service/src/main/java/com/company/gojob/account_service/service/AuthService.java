package com.company.gojob.account_service.service;

import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.repository.AuthRepository;
import com.company.gojob.account_service.service.implement.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService implements IAuthService {
    @Autowired
    AuthRepository authRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtService jwtService;

    @Override
    public boolean createUser(UserCredential userCredential) {
        userCredential.setPassword(passwordEncoder.encode(userCredential.getPassword()));
        authRepository.save(userCredential);
        return true;
    }

    public String generateToken(String userName){
        return jwtService.generateToken(userName);
    }

    public Map<String, Object> generateTokenWithExpiration(String userName) {
        String token = jwtService.generateToken(userName);
        Date expirationDate = jwtService.getExpirationDateFromToken(token);

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", token);
        tokenData.put("expirationDate", expirationDate);

        return tokenData;
    }

    public void invalidateToken(String token){
        jwtService.validateToken(token);
    }


}
