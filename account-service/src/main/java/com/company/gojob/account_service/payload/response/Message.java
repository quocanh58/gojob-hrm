package com.company.gojob.account_service.payload.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
public class Message {
    private ArrayList<String> messages;
}
