package com.company.gojob.account_service.controller;

import com.company.gojob.account_service.constant.ApiEndpoints;
import com.company.gojob.account_service.dto.UserCredentialDTO;
import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.payload.request.AuthRequest;
import com.company.gojob.account_service.payload.response.AuthResponse;
import com.company.gojob.account_service.payload.response.LoginResponse;
import com.company.gojob.account_service.service.AuthService;
import com.company.gojob.account_service.service.JwtService;
import com.company.gojob.account_service.service.UserCredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin("*")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    private UserCredentialService userCredentialService;
    private String token;

    @PostMapping(ApiEndpoints.AUTH_REGISTER)
    public String addNewUser(@RequestBody UserCredential user) {
        return authService.createUser(user);
    }


    @PostMapping(ApiEndpoints.AUTH_GENERATE_TOKEN)
    public String getToken(@RequestBody AuthRequest request) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        if (authentication.isAuthenticated()) {
            return authService.generateToken(request.getUsername());
        }
        throw new RuntimeException("Authentication is not authenticated.");
    }

    @PostMapping(ApiEndpoints.AUTH_LOGIN)
    public ResponseEntity<LoginResponse> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            if (authentication.isAuthenticated()) {
                // Lấy accessToken
                String accessToken = authService.generateToken(request.getUsername());

                // Lấy refreshToken
                String refreshToken = jwtService.generateRefreshToken(request.getUsername());

                // Lấy expirationDate
                Map<String, Object> tokenData = authService.generateTokenWithExpiration(request.getUsername());
                Date expirationDate = (Date) tokenData.get("expirationDate");

                // Tạo header với status
                HttpHeaders headers = new HttpHeaders();
                headers.set("status", "200");

                // Lấy thông tin user
                UserCredentialDTO userCredentialResponse = userCredentialService.getUserCredentialByUserName(request.getUsername());

                // Tạo LoginResponseData
                LoginResponse.DataResponse dataResponse = new LoginResponse.DataResponse(
                        accessToken,
                        refreshToken,
                        expirationDate.toString(),
                        userCredentialResponse
                );

                LoginResponse loginResponse = new LoginResponse(true, dataResponse, "Login Successfully.");

                // Trả về kết quả thành công
                return ResponseEntity.ok().body(loginResponse);
            }
        } catch (Exception ex) {
            // Trường hợp thất bại, ví dụ: Sai username/password
            HttpHeaders headers = new HttpHeaders();
            headers.set("status", "401");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null, "Authorization failed."));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("status", "403");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new LoginResponse(false, null, "Login failed in FORBIDDEN"));
    }

    @PostMapping(ApiEndpoints.AUTH_REFRESH_TOKEN)
    public ResponseEntity<AuthResponse<Map<String, String>>> refreshToken(@RequestBody Map<String, String> tokenRequest) {
        String refreshToken = tokenRequest.get("refreshToken");

        // Kiểm tra refreshToken có hợp lệ không
        if (refreshToken == null || jwtService.isTokenExpired(refreshToken)) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("status", "401");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .body(new AuthResponse<>(401, "Invalid or expired refresh token", null));
        }

        // Tạo accessToken mới từ refreshToken
        String username = jwtService.extractClaims(refreshToken).getSubject();
        String newAccessToken = jwtService.generateToken(username);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", refreshToken);

        // Gửi response status 200 lên header
        HttpHeaders headers = new HttpHeaders();
        headers.set("status", "200");

        return ResponseEntity.ok()
                .headers(headers)
                .body(new AuthResponse<>(200, "Success", tokens));
    }


    @GetMapping(ApiEndpoints.AUTH_VALIDATE_TOKEN)
    public String validateToken(@RequestParam("token") String token) {
        authService.invalidateToken(token);
        return "Token is valid";
    }


    @GetMapping(ApiEndpoints.GET_ALL_USERS)
    public ResponseEntity<List<UserCredential>> getAllUsers() {
        try {
            HttpHeaders headers = new HttpHeaders();
            List<UserCredential> listUser = userCredentialService.getAllUserCredentials();

            if (listUser.isEmpty()) {
                headers.add("status", "404");

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .headers(headers)
                        .body(null);
            }

            headers.add("status", "200");

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .body(listUser);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }

//    @GetMapping(ApiEndpoints.GET_ALL_USERS_V2)
//    public ResponseEntity<PaginationResponse<List<UserCredential>>> getAllUsersV2(@RequestParam(value = "page", defaultValue = "1") int page,
//                                                                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
//        HttpHeaders headers = new HttpHeaders();
//        try {
//            // Lấy danh sách người dùng từ dịch vụ
//            List<UserCredential> listUser = userCredentialService.getAllUserCredentials();
//
//            if (listUser.isEmpty()) {
//                headers.add("status", "404");
//
//                PaginationResponse<List<UserCredential>> emptyResponse = new PaginationResponse<>(
//                        false,
//                        new PaginationResponse.Message(new ArrayList<>()),
//                        new ArrayList<>(),
//                        "List user is empty.",
//                        new PaginationResponse.Paginate(0, page, pageSize, 0),
//                        new PaginationResponse.ExtraData()
//                );
//
//                return ResponseEntity
//                        .status(HttpStatus.NOT_FOUND)
//                        .headers(headers)
//                        .body(null);
//            } else {
//                // Tính toán phân trang
//                int totalRecords = listUser.size();
//                int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
//                int startIndex = (page - 1) * pageSize;
//                int endIndex = Math.min(startIndex + pageSize, totalRecords);
//
//                // Tạo đối tượng Paginate
//                PaginationResponse.Paginate paginate = new PaginationResponse.Paginate();
//                paginate.totalRecords = totalRecords;
//                paginate.page = page;
//                paginate.pageSize = pageSize;
//                paginate.totalPages = totalPages;
//
//                if (startIndex >= totalRecords) {
//                    headers.add("status", "400");
//
//                    PaginationResponse<List<UserCredential>> invalidPageResponse = new PaginationResponse<>(
//                            false,
//                            new PaginationResponse.Message(new ArrayList<>()),
//                            new ArrayList<>(),
//                            "Invalid page number",
//                            new PaginationResponse.Paginate(totalRecords, page, pageSize, totalPages),
//                            new PaginationResponse.ExtraData()
//                    );
//
//                    return ResponseEntity
//                            .status(HttpStatus.BAD_REQUEST)
//                            .headers(headers)
//                            .body(invalidPageResponse);
//                }
//
//                List<UserCredential> paginatedUsers = listUser.subList(startIndex, endIndex);
//
//                // Tạo đối tượng Paginate
//                PaginationResponse.Paginate paginateResponse = new PaginationResponse.Paginate(
//                        totalRecords,
//                        page,
//                        pageSize,
//                        totalPages
//                );
//
//                // Tạo đối tượng phản hồi
//                PaginationResponse<List<UserCredential>> response = new PaginationResponse<>(
//                        true,
//                        new PaginationResponse.Message(new ArrayList<>(List.of("Load data successfully."))), // Nếu cần thêm email vào message
//                        paginatedUsers,
//                        "Fetched successfully",
//                        paginate,
//                        new PaginationResponse.ExtraData()
//                );
//
//                headers.add("status", "200");
//                return ResponseEntity
//                        .status(HttpStatus.OK)
//                        .headers(headers)
//                        .body(response);
//            }
//
//        } catch (Exception e) {
//            PaginationResponse<List<UserCredential>> errorResponse = new PaginationResponse<>(
//                    false,
//                    new PaginationResponse.Message(new ArrayList<>()),
//                    null,
//                    "An error occurred: " + e.getMessage(),
//                    null,
//                    new PaginationResponse.ExtraData()
//            );
//            headers.add("status", "400");
//            return ResponseEntity
//                    .status(HttpStatus.BAD_REQUEST)
//                    .headers(headers)
//                    .body(errorResponse);
//        }
//    }

    @GetMapping(ApiEndpoints.GET_USER_BY_ID)
    public ResponseEntity<UserCredential> getUserById(@PathVariable String id) {
        try {
            // Validate JWT token

//            authService.invalidateToken(authentication);

            // Lấy thông tin người dùng từ service
            UserCredential user = userCredentialService.getUserCredentialById(id);
            HttpHeaders headers = new HttpHeaders();
            headers.add("status", "success");

            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)  // 404 nếu không tìm thấy user
                        .headers(headers)
                        .build();
            }

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(user); // 200 và trả thông tin user

        } catch (Exception e) {
            // Nếu token không hợp lệ hoặc hết hạn, trả về lỗi 401 Unauthorized
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }


}
