package com.company.gojob.account_service.mapper;

import com.company.gojob.account_service.dto.UserCredentialDTO;
import com.company.gojob.account_service.model.UserCredential;

public class UserCredentialMapper {
    public static UserCredential toEntity(UserCredentialDTO dto) {
        return new UserCredential(
                null, // ID sẽ được tự động tạo
                dto.getUsername(),
                dto.getPassword(),
                dto.getEmail()
        );
    }
}
