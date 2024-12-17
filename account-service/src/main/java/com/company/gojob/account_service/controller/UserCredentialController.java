//package com.company.gojob.account_service.controller;
//
//import com.company.gojob.account_service.constant.ApiEndpoints;
//import com.company.gojob.account_service.model.UserCredential;
//import com.company.gojob.account_service.service.JwtService;
//import com.company.gojob.account_service.service.UserCredentialService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping(ApiEndpoints.BASE_URL)
//public class UserCredentialController {
//
//    @Autowired
//    JwtService jwtService;
//
//    @Autowired
//    private UserCredentialService userCredentialService;
//
//    @GetMapping(ApiEndpoints.GET_ALL_USERS)
//    public ResponseEntity<List<UserCredential>> getAllUsers(@RequestHeader("Authorization") String token) {
//        try {
//            // Validate JWT token
//            jwtService.validateToken(token); // Kiểm tra tính hợp lệ của token
//
//            List<UserCredential> users = userCredentialService.getAllUserCredentials();
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("status", "success");
//
//            if (users.isEmpty()) {
//                return ResponseEntity
//                        .status(HttpStatus.NOT_FOUND)  // 404 nếu không tìm thấy user
//                        .headers(headers)
//                        .body(null);
//            }
//
//            return ResponseEntity
//                    .ok()
//                    .headers(headers)
//                    .body(users);
//
//        } catch (Exception e) {
//            // Nếu token không hợp lệ, trả về lỗi 401
//            return ResponseEntity
//                    .status(HttpStatus.UNAUTHORIZED)
//                    .body(null);
//        }
//    }
//
//    @GetMapping(ApiEndpoints.GET_USER_BY_ID)
//    public ResponseEntity<UserCredential> getUserById(@RequestHeader("Authorization") String token, @PathVariable String id) {
//        try {
//            // Validate JWT token
//            jwtService.validateToken(token); // Kiểm tra tính hợp lệ của token
//
//            // Lấy thông tin người dùng từ service
//            UserCredential user = userCredentialService.getUserCredentialById(id);
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("status", "success");
//
//            if (user == null) {
//                return ResponseEntity
//                        .status(HttpStatus.NOT_FOUND)  // 404 nếu không tìm thấy user
//                        .headers(headers)
//                        .build();
//            }
//
//            return ResponseEntity
//                    .ok()
//                    .headers(headers)
//                    .body(user); // 200 và trả thông tin user
//
//        } catch (Exception e) {
//            // Nếu token không hợp lệ hoặc hết hạn, trả về lỗi 401 Unauthorized
//            return ResponseEntity
//                    .status(HttpStatus.UNAUTHORIZED)
//                    .body(null);
//        }
//    }
//}
