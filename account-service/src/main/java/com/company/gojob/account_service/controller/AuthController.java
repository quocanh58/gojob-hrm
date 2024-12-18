package com.company.gojob.account_service.controller;

import com.company.gojob.account_service.constant.ApiEndpoints;
import com.company.gojob.account_service.dto.UserCredentialDTO;
import com.company.gojob.account_service.mapper.UserCredentialMapper;
import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.payload.request.AuthRequest;
import com.company.gojob.account_service.payload.request.UpdateUserRequest;
import com.company.gojob.account_service.payload.response.*;
import com.company.gojob.account_service.service.AuthService;
import com.company.gojob.account_service.service.JwtService;
import com.company.gojob.account_service.service.UserCredentialService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerExceptionResolver;

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
    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

    @PostMapping(ApiEndpoints.AUTH_REGISTER)
    public ResponseEntity<CommonResponse<UserCredentialDTO>> addNewUser(@Valid @RequestBody UserCredentialDTO userDTO) {
        try {
            // Map DTO to Entity
            UserCredential user = UserCredentialMapper.toEntity(userDTO);
            boolean isCreate = authService.createUser(user);

            if (isCreate){
                HttpHeaders header = new HttpHeaders();
                header.set("status", "200");

                CommonResponse<UserCredentialDTO> response = new CommonResponse<>(
                        true,
                        "User created successfully.",
                        userDTO
                );

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .headers(header)
                        .body(response);
            }

        } catch (Exception e) {
            // Tạo đối tượng phản hồi khi có lỗi
            CommonResponse<UserCredentialDTO> errorResponse = new CommonResponse<>(
                    false,
                    "Failed to create user: " + e.getMessage(),
                    null
            );
            HttpHeaders header = new HttpHeaders();
            header.set("status", "400");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
        }

        CommonResponse<UserCredentialDTO> errorResponse = new CommonResponse<>(
                false,
                "Failed to create user: ",
                null
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
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

    @GetMapping(ApiEndpoints.GET_ALL_USERS_V2)
    public ResponseEntity<PaginationResponse<List<UserCredential>>> getAllUsersV2(@RequestParam(value = "page", defaultValue = "1") int page,
                                                                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        HttpHeaders headers = new HttpHeaders();
        try {
            // Lấy danh sách người dùng từ dịch vụ
            List<UserCredential> listUser = userCredentialService.getAllUserCredentials();

            if (listUser.isEmpty()) {
                headers.add("status", "404");

                PaginationResponse<List<UserCredential>> emptyResponse =
                        PaginationResponse.<List<UserCredential>>builder()
                                .success(false)
                                .message("List user is empty")
                                .data(new ArrayList<>())
                                .devMessage("List user is empty.")
                                .paginate(new Paginate(0, page, pageSize, 0))
                                .extraData(null)
                                .build();

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .headers(headers)
                        .body(null);
            } else {
                // Tính toán phân trang
                int totalRecords = listUser.size();
                int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
                int startIndex = (page - 1) * pageSize;
                int endIndex = Math.min(startIndex + pageSize, totalRecords);

                // Tạo đối tượng Paginate
                Paginate paginate = new Paginate();
                paginate.totalRecords = totalRecords;
                paginate.page = page;
                paginate.pageSize = pageSize;
                paginate.totalPages = totalPages;

                if (startIndex >= totalRecords) {
                    headers.add("status", "400");

                    PaginationResponse<List<UserCredential>> invalidPageResponse =
                            PaginationResponse.<List<UserCredential>>builder()
                                    .success(false)
                                    .message("Get data failed.")
                                    .data(new ArrayList<>())
                                    .devMessage("Invalid page number")
                                    .paginate(new Paginate(totalRecords, page, pageSize, totalPages))
                                    .extraData(null)
                                    .build();

                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .headers(headers)
                            .body(invalidPageResponse);
                }

                List<UserCredential> paginatedUsers = listUser.subList(startIndex, endIndex);

                // Tạo đối tượng Paginate
                Paginate paginateResponse = new Paginate(
                        totalRecords,
                        page,
                        pageSize,
                        totalPages
                );

                // Tạo đối tượng phản hồi
                PaginationResponse<List<UserCredential>> response =

                        PaginationResponse.<List<UserCredential>>builder()
                                .success(true)
                                .message("Get data successfully.")
                                .data(paginatedUsers)
                                .devMessage("Fetched successfully")
                                .paginate(paginate)
                                .extraData(null)
                                .build();

                headers.add("status", "200");
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .headers(headers)
                        .body(response);
            }

        } catch (Exception e) {
            PaginationResponse<List<UserCredential>> errorResponse =
                    PaginationResponse.<List<UserCredential>>builder()
                            .success(false)
                            .message("Get data failed.")
                            .data(null)
                            .devMessage("")
                            .paginate(null)
                            .extraData(null)
                            .build();
            headers.add("status", "400");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .headers(headers)
                    .body(errorResponse);
        }
    }

    @GetMapping(ApiEndpoints.GET_USER_BY_ID)
    public ResponseEntity<UserCredential> getUserById(@PathVariable String id) {
        try {
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

    @PutMapping(ApiEndpoints.UPDATE_USER)
    public ResponseEntity<UserCredentialResponse> updateUser(@PathVariable String id, @Valid @RequestBody UpdateUserRequest request) {
        try {
            if (request.getUsername() != null || request.getUsername() != null) {
                // Cập nhật thông tin người dùng từ service
                int isSuccess = userCredentialService.updateUserCredentialById(id, request.getEmail(), request.getUsername());

                if (isSuccess > 0) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("status", "200");
                    UserCredentialResponse response = new UserCredentialResponse(id, request.getEmail(), request.getUsername());

                    return ResponseEntity
                            .ok()
                            .headers(headers)
                            .body(response);
                } else {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("status", "400");
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .headers(headers)
                            .body(new UserCredentialResponse(id, null, null));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("status", "400");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .headers(headers)
                .body(new UserCredentialResponse(id, null, null));
    }

    @DeleteMapping(ApiEndpoints.DELETE_USER)
    public ResponseEntity<CommonResponse<?>> deleteUser(@PathVariable String id) {
        HttpHeaders headers = new HttpHeaders();

        try {
            UserCredential user = userCredentialService.getUserCredentialById(id);
            // Xóa người dùng từ service
            int isSuccess = userCredentialService.deleteUserCredentialById(id);

            if (isSuccess > 0) {
                headers.add("status", "200");

                Map<String, String> body = new HashMap<String, String>();
                body.put("userId", user.getId());
                body.put("username", user.getUsername());
                body.put("email", user.getEmail());
                return ResponseEntity
                        .ok()
                        .headers(headers)
                        .body(new CommonResponse<>(true, "Delete user success", body));
            } else {
                headers.add("status", "400");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .headers(headers)
                        .body(new CommonResponse<>(true, "Delete user failed.", null));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }
}
