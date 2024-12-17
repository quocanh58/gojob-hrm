package com.company.gojob.account_service.controller;

import com.company.gojob.account_service.constant.ApiEndpoints;
import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.payload.request.AuthRequest;
import com.company.gojob.account_service.payload.response.AuthResponse;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    private UserCredentialService userCredentialService;

    @PostMapping("/register")
    public String addNewUser(@RequestBody UserCredential user){
        return authService.createUser(user);
    }

    @PostMapping("/token")
    public String getToken(@RequestBody AuthRequest request){

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        if (authentication.isAuthenticated()){
            return authService.generateToken(request.getUsername());
        }
        throw new RuntimeException("Authentication is not authenticated.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse<Map<String, String>>> login(@RequestBody AuthRequest request) {
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

            // Trả về ResponseEntity
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new AuthResponse<>(200, "Success", tokens));
        }

        // Trường hợp thất bại
        HttpHeaders headers = new HttpHeaders();
        headers.set("status", "401");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .headers(headers)
                .body(new AuthResponse<>(401, "Authentication failed", null));
    }

    @PostMapping("/refresh")
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


    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token){
         authService.invalidateToken(token);
         return "Token is valid";
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserCredential>> getAllUsers(@RequestHeader("Authorization") String token) {
        try {

            // Validate JWT token
            authService.invalidateToken(token);

            List<UserCredential> users = userCredentialService.getAllUserCredentials();
            HttpHeaders headers = new HttpHeaders();
            headers.add("status", "success");

            if (users.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .headers(headers)
                        .body(null);
            }

            return ResponseEntity.ok().headers(headers).body(users);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserCredential> getUserById(@RequestHeader("Authorization") String token, @PathVariable String id) {
        try {
            // Validate JWT token
            authService.invalidateToken(token); // Kiểm tra tính hợp lệ của token

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
