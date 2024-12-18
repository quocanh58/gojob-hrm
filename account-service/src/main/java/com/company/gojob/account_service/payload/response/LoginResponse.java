package com.company.gojob.account_service.payload.response;

import com.company.gojob.account_service.dto.UserCredentialDTO;
import com.company.gojob.account_service.model.UserCredential;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private boolean success;
    private DataResponse data;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataResponse {
        private String accessToken;
        private String refreshToken;
        private String expiredDate;
        private UserCredentialDTO userCredentialResponse;

    }
}
