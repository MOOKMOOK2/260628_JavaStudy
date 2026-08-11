package com.example.javastudy.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.javastudy.auth.dto.SignupRequest;

import org.springframework.ui.Model;

/*
 * AuthPageController
 *
 * 로그인/회원가입 화면을 보여주는 페이지 전용 컨트롤러다.
 *
 * 역할:
 * - HTML 화면만 반환한다.
 * - 실제 회원가입/로그인 처리 로직은 하지 않는다.
 * - 화면만 보여주고, 제출은 별도의 AuthController가 처리한다.
 */
@Controller
@RequestMapping("/auth")
public class AuthPageController {

    /*
     * 로그인 페이지를 보여준다.
     *
     * 반환값 "auth/login"은
     * src/main/resources/templates/auth/login.html
     * 파일을 의미한다.
     */
    @GetMapping
    public String loginPage() {
        return "auth/login";
    }

    /*
     * 회원가입 페이지를 보여준다.
     *
     * 반환값 "auth/signup"은
     * src/main/resources/templates/auth/signup.html
     * 파일을 의미한다.
     */
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("request", new SignupRequest()); //signup.html에서 form
        return "auth/signup";
    }
}