package com.company.gojob.account_service.service;

import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.repository.UserCredentialRepository;
import com.company.gojob.account_service.service.implement.IUserCredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserCredentialService implements IUserCredentialService {
    @Autowired
    UserCredentialRepository userCredentialRepository;


    @Override
    public List<UserCredential> getAllUserCredentials() {
        return userCredentialRepository.findAllUserCredentials();
    }

    @Override
    public UserCredential getUserCredentialById(String id) {
        return userCredentialRepository.findUserCredentialById(id);
    }
}
