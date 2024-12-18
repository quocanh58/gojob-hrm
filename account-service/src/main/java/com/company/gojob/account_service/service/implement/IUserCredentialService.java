package com.company.gojob.account_service.service.implement;

import com.company.gojob.account_service.dto.UserCredentialDTO;
import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.payload.response.UserCredentialResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IUserCredentialService {
    List<UserCredential> getAllUserCredentials();

    UserCredential getUserCredentialById(String id);

    UserCredentialDTO getUserCredentialByUserName(String username);
}
