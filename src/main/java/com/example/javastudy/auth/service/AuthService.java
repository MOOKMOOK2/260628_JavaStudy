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

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce.Cluster.Refresh;
import org.springframework.http.HttpEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    //대상이 누구든지 안바뀌고 사용하는 공통 도구
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

    public void validateRefreshToken(String refreshToken) {
        if(refreshToken==null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("리프레시 토큰이 없습니다."); //컨트롤러 예외로 감
        }
        if(!jwtTokenProvider.isValid(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 유효하지 않습니다.");
        } 
        if(!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw new IllegalArgumentException("리프레시 토큰이 아닙니다.");
        }
        RefreshToken/* Entity */ savedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow/* optional이 없는경우 */(() -> new IllegalArgumentException("DB에 리프레시 토큰이 존재하지 않습니다."));
        if(savedRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            //엔티티(DB)의 리프레시토큰의 만료시각과 현재시각 비교
            throw new IllegalArgumentException("리프레시 토큰이 만료되었습니다.");
        }

    }

    public String refreshAccessToken(String refreshToken) {
        //userId를 엔티티가 아니라 refreshToken으로 가져오는게 핵심
        Long userId = jwtTokenProvider.getUserId((refreshToken));
        if(userId == null) {
            throw new IllegalArgumentException("사용자 ID가 존재하지 않습니다.");
        }
        User user = userRepository.findById(userId).orElseThrow(()
            -> new IllegalArgumentException("사용자 정보가 유효하지 않습니다."));
        String userEmail = user.getEmail();
        String userRole = user.getRole();
        
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, userEmail, userRole);
        return newAccessToken;
    }
}