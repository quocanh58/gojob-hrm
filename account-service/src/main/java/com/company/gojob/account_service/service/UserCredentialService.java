package com.company.gojob.account_service.service;

import com.company.gojob.account_service.dto.UserCredentialDTO;
import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.payload.response.UserCredentialResponse;
import com.company.gojob.account_service.repository.UserCredentialRepository;
import com.company.gojob.account_service.service.implement.IUserCredentialService;
import org.modelmapper.ModelMapper;
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

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<UserCredential> getAllUserCredentials() {
        return userCredentialRepository.findAllUserCredentials();
    }

    @Override
    public UserCredential getUserCredentialById(String id) {
        return userCredentialRepository.findUserCredentialById(id);
    }

    @Override
    public UserCredentialDTO getUserCredentialByUserName(String username) {
        UserCredential userCredential = userCredentialRepository.findUserCredentialByUsername(username);
        if (userCredential == null) {
            return null;
        }
        return modelMapper.map(userCredential, UserCredentialDTO.class);
    }

    @Override
    public int updateUserCredentialById(String id, String email, String username) {
        return userCredentialRepository.updateUserCredentialById(id, email, username);
    }

    @Override
    public int deleteUserCredentialById(String id) {
        return userCredentialRepository.deleteUserCredentialById(id);
    }
}
