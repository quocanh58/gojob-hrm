package com.company.gojob.account_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCredentialDTO {
    private String id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
}
