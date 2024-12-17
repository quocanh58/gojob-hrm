package com.company.gojob.account_service.payload.response;

import com.company.gojob.account_service.model.UserCredential;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialResponse {
    private boolean success;
    private Data data;
    private String message;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String accessToken;
        private String refreshToken;
        private Date expiredDate;
        private UserCredential user;
    }
}
