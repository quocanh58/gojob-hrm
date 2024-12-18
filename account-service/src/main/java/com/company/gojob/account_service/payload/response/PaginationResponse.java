package com.company.gojob.account_service.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;


@Data
@Builder
public class PaginationResponse<T> implements Serializable {
    public boolean success;
    public Object message;
    public T data;
    public String devMessage;
    public Paginate paginate;
    public ExtraData extraData;
}




