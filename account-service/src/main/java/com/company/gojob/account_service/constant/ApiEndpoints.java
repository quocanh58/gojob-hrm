package com.company.gojob.account_service.constant;

public class ApiEndpoints {
    // Endpoint cho UserCredential
    public static final String BASE_URL = "/api/users";
    public static final String GET_ALL_USERS = BASE_URL;  // GET /api/users
    public static final String GET_USER_BY_ID = BASE_URL + "/{id}";  // GET /api/users/{id}
    public static final String CREATE_USER = BASE_URL;  // POST /api/users
    public static final String UPDATE_USER = BASE_URL + "/{id}";  // PUT /api/users/{id}
    public static final String DELETE_USER = BASE_URL + "/{id}";  // DELETE /api/users/{id}

    // Endpoint othres
}
