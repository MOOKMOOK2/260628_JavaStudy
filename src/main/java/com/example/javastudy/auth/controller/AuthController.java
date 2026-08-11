package com.example.javastudy.auth.controller;

import com.example.javastudy.auth.dto.LoginRequest;
import com.example.javastudy.auth.dto.LoginResponse;
import com.example.javastudy.auth.dto.SignupRequest;
import com.example.javastudy.auth.service.AuthService;
import com.example.javastudy.global.config.JwtProperties;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/*
 * AuthController
 *
 * 회원가입/로그인 form 요청을 받는 HTTP 입구다.
 *
 * Controller는 얇게 유지한다.
 * 실제 로직은 AuthService에 맡기고,
 * 여기서는 form 데이터를 받아 서비스로 넘기고
 * 처리 결과에 따라 redirect 한다.
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /*
     * AuthService
     *
     * 회원가입/로그인 비즈니스 로직을 처리하는 서비스다.
     * 생성자 주입으로 연결된다.
     * Spring이 자동으로 주입해준다.
     */
    private final AuthService authService;
    private final JwtProperties jwtProperties;
    /*
     * 회원가입 form 처리
     *
     * POST /auth/signup
     *
     * @Valid
     * - form 값 검증 수행
     *
     * @RequestBody
     * - 현재는 사용하지 않는다
     *
     * @ModelAttribute
     * - HTML form의 입력값을 DTO에 바인딩한다
     */

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupRequest request, //dto
        BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            return "auth/signup";
        }
        try {
            authService.signup(request);
            return "redirect:/auth";
        } catch(IllegalArgumentException  e) {
            model.addAttribute("errorMessage", "회원가입에 실패했습니다.");
            return "auth/signup";
        }
    }

    /*
     * 로그인 form 처리
     *
     * POST /auth
     *
     * form 값 검증 후 로그인 처리만 하고
     * 성공 시 메인 페이지로 redirect 한다.
     */
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
        } catch(IllegalArgumentException err) {
            model.addAttribute("errorMessage", err.getMessage());
            return "auth/login"; //login.html을 의미함
        }
    }
}
