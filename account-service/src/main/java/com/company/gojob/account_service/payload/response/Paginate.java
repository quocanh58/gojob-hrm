package com.company.gojob.account_service.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paginate implements Serializable {
    public int totalRecords;
    public int page;
    public int pageSize;
    public int totalPages;
}
