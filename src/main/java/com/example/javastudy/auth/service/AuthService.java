package com.example.javastudy.auth.service;

import com.example.javastudy.auth.dto.LoginRequest;
import com.example.javastudy.auth.dto.LoginResponse;
import com.example.javastudy.auth.dto.SignupRequest;
import com.example.javastudy.auth.entity.RefreshToken;
import com.example.javastudy.auth.jwt.JwtTokenProvider;
import com.example.javastudy.auth.repository.RefreshTokenRepository;
import com.example.javastudy.global.config.JwtProperties;
import com.example.javastudy.user.entity.User;
import com.example.javastudy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getEmail(),
                encodedPassword,
                request.getNickname(),
                "ROLE_USER"
        );

        userRepository.save(user); //db저장
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken =  jwtTokenProvider.createAccessToken(
            user.getId(), user.getEmail(), user.getRole()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        
        refreshTokenRepository.save( //save는 엔티티 객체 자체를 인자로 받는다
            new RefreshToken(
                user.getId(), refreshToken,
                LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration()))
                //지금시각에서 정해놓은 만료시간을 jwtProperties에서 가져와서 더함 = 해당 토큰 만료시각
            )
        );

        return new LoginResponse(accessToken, refreshToken);
    }
}