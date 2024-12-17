package com.company.gojob.account_service.service.implement;

import com.company.gojob.account_service.model.UserCredential;
import org.springframework.stereotype.Service;

@Service
public interface IAuthService {
    public String createUser(UserCredential userCredential);
}
