package com.example.javastudy.auth.controller;

import com.example.javastudy.auth.dto.LoginRequest;
import com.example.javastudy.auth.dto.LoginResponse;
import com.example.javastudy.auth.dto.SignupRequest;
import com.example.javastudy.auth.service.AuthService;
import com.example.javastudy.global.config.JwtProperties;
import com.example.javastudy.global.exception.DuplicateEmailException;
import com.example.javastudy.global.exception.InvalidRefreshTokenException;
import com.example.javastudy.global.exception.LoginFailedException;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtProperties jwtProperties;
    
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupRequest request, //dto
        BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            return "auth/signup";
        }
        try {
            authService.signup(request);
            return "redirect:/auth";
        } catch(DuplicateEmailException  e) {
            model.addAttribute("errorMessage", "회원가입에 실패했습니다.");
            return "auth/signup";
        }
    }

    @PostMapping
    public String login(@Valid @ModelAttribute LoginRequest request, BindingResult bindingResult, Model model, HttpServletResponse response) {
        //@ModelAttribute는 HTML form으로 들어온 값을 LoginRequest 객체에 넣어줌 -> Valid는 그것들에 대한 검증
        if(bindingResult.hasErrors()) return "auth/login";

        try {
            LoginResponse tokens = authService.login(request);
            String accessToken = tokens.getAccessToken();
            String refreshToken = tokens.getRefreshToken();
            ResponseCookie accessCookie = ResponseCookie
                .from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(jwtProperties.getAccessTokenExpiration()))
                .sameSite("Lax")
                .build();
            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

            ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", refreshToken)//외부 사이트에서 오는 일부 요청에 쿠키 전송 제한
                .httpOnly(true)//JavaScript 접근 차단
                .secure(false)//로컬 HTTP에서도 전송 허용, 배포 환경 HTTPS에서는 true
                .path("/")//사이트 전체 경로에서 쿠키 전송
                .maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration()))//쿠키 유효기간 14일
                .sameSite("Lax")//외부 사이트에서 오는 일부 요청에 쿠키 전송 제한
                .build();//지금까지 설정한 걸 실제 쿠키 객체로 완성
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            return "redirect:/";
        } catch(LoginFailedException err) {
            model.addAttribute("errorMessage", err.getMessage());
            return "auth/login"; //login.html을 의미함
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        //ResponseEntity<String> : HTTP전체 응답 + body 는 String
        String refreshToken = getRefreshTokenFromCookie(request);
        authService.validateRefreshToken(refreshToken);
        String accessToken = authService.refreshAccessToken(refreshToken);

        ResponseCookie accessCookie = ResponseCookie
            .from("accessToken", accessToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(Duration.ofMillis(jwtProperties.getAccessTokenExpiration()))
            .sameSite("Lax")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        
        return ResponseEntity.ok("Access token refreshed");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookie(request);
        authService.logout(refreshToken);

        ResponseCookie deleteAccessCookie = ResponseCookie.from("accessToken", "")
            .path("/")
            .maxAge(0)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString());

        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refreshToken", "")
            .path("/")
            .maxAge(0)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString());
            
        return ResponseEntity.ok("logout success");

    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
