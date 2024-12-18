package com.company.gojob.account_service.constant;

public class ApiEndpoints {

    // Endpoint BASE_URL
    public static final String BASE_URL = "/api/v1";

    // Endpoint cho Authorization
    public static final String AUTH_URL = BASE_URL + "/auth";
    public static final String AUTH_REGISTER = AUTH_URL + "/register";
    public static final String AUTH_LOGIN = AUTH_URL + "/login";
    public static final String AUTH_GENERATE_TOKEN = AUTH_URL + "/token";
    public static final String AUTH_REFRESH_TOKEN = AUTH_URL + "/refresh";
    public static final String AUTH_VALIDATE_TOKEN = AUTH_URL + "/validate";

    // Endpoint cho UserCredential
    public static final String USER_URL = BASE_URL + "/user";
    public static final String GET_ALL_USERS = USER_URL;                    // GET /api/users
    public static final String GET_ALL_USERS_V2 = USER_URL + "/paginate";   // GET /api/users/paginate
    public static final String GET_USER_BY_ID = USER_URL + "/{id}";         // GET /api/users/{id}
    public static final String CREATE_USER = USER_URL;                      // POST /api/users
    public static final String UPDATE_USER = USER_URL + "/{id}";            // PUT /api/v1/users/{id}
    public static final String DELETE_USER = USER_URL + "/{id}";            // DELETE /api/users/{id}

}
