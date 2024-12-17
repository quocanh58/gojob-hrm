package com.company.gojob.account_service.controller;

import com.company.gojob.account_service.constant.ApiEndpoints;
import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.payload.request.AuthRequest;
import com.company.gojob.account_service.payload.response.AuthResponse;
import com.company.gojob.account_service.service.AuthService;
import com.company.gojob.account_service.service.JwtService;
import com.company.gojob.account_service.service.UserCredentialService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public ResponseEntity<AuthResponse<Map<String, String>>> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            if (authentication.isAuthenticated()) {
                String accessToken = authService.generateToken(request.getUsername());
                String refreshToken = jwtService.generateRefreshToken(request.getUsername());

                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", accessToken);
                tokens.put("refreshToken", refreshToken);

                // Tạo header với status
                HttpHeaders headers = new HttpHeaders();
                headers.set("status", "200");

                // Trả về kết quả thành công
                return ResponseEntity.ok(new AuthResponse<>(200, "Success", tokens));
            }
        } catch (Exception ex) {
            // Trường hợp thất bại, ví dụ: Sai username/password
            HttpHeaders headers = new HttpHeaders();
            headers.set("status", "401");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse<>(401, "UNAUTHORIZED", new HashMap<>()));
        }

        // Trường hợp thất bại
        HttpHeaders headers = new HttpHeaders();
        headers.set("status", "403");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new AuthResponse<>(403, "FORBIDDEN", new HashMap<>()));
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

            // Lấy thông tin token từ SecurityContext


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
