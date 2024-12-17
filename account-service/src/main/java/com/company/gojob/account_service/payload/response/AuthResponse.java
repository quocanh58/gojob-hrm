package com.company.gojob.account_service.payload.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuthResponse<T> {
    private int status;
    private String message;
    private T data;
}
