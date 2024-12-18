package com.company.gojob.account_service.service.implement;

import com.company.gojob.account_service.dto.UserCredentialDTO;
import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.payload.request.UpdateUserRequest;
import com.company.gojob.account_service.payload.response.UserCredentialResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IUserCredentialService {
    List<UserCredential> getAllUserCredentials();

    UserCredential getUserCredentialById(String id);

    UserCredentialDTO getUserCredentialByUserName(String username);

    int updateUserCredentialById(String id, String email, String username);

    int deleteUserCredentialById(String id);
}
