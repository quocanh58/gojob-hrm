package com.company.gojob.account_service.filter;

import com.company.gojob.account_service.payload.response.AuthResponse;
import com.company.gojob.account_service.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null) {
            try {
                // Kiểm tra xem token có hết hạn hay không
                if (jwtService.isTokenExpired(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    sendErrorResponse(response, "Token expired");
                    return;
                }

                // Lấy thông tin từ token và tạo một đối tượng xác thực
                String username = jwtService.extractClaims(token).getSubject();
                if (username != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                sendErrorResponse(response, "Invalid token");
                return;
            }
        }

        filterChain.doFilter(request, response); // Tiến hành chuỗi filter
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Lấy phần sau "Bearer "
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        // Tạo và trả về một AuthResponse khi token hết hạn hoặc không hợp lệ
        AuthResponse<Object> authResponse = new AuthResponse<>(401, message, null);

        // Set content type và trả về AuthResponse trong body
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        writer.write(new ObjectMapper().writeValueAsString(authResponse));
        writer.flush();
    }
}
