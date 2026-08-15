package com.example.javastudy.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.time.Instant;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.example.javastudy.global.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component //이 클래스를 spring bean으로 등록해줘 라는 뜻
@RequiredArgsConstructor //final 필드를 매개변수로 받는 생성자를 Lombok이 자동으로 만들어주는 어노테이션
//jwtProperties 변수에 값을 넣어줌
public class JwtTokenProvider {
        private final JwtProperties jwtProperties; //jwt.* 들어감, getter로 꺼내쓴다
        private SecretKey secretKey; //init에서 값을 넣어준다 / SecretKey 타입이여야 실제 서명에 쓸수있다
    
    @PostConstruct //Spring이 JwtTokenProvider Bean을 만든 직후에, 이 메서드를 자동으로 한 번 실행하라는 뜻
    public void init() { //application.properties에 있는 jwt.secret 문자열을 JWT 서명에 쓸 수 있는 SecretKey 객체로 변환해서 secretKey 필드에 저장하는 것
        //this.secretKey = Jwts.SIG.HS256.key().build(); //랜덤으로 새 시크릿 키 하나 만든다
        this.secretKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
            jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        ); //properties의 jwt.secret 값을 byte[]로 변환한 뒤 HMAC 서명용 SecretKey로 생성해 저장
    }

    public String createAccessToken(Long userId, String email, String role) {
        return createToken(
                userId,
                email,
                role,
                jwtProperties.getAccessTokenExpiration(),
                "access"
        );
    }

    public String createRefreshToken(Long userId) { //AccessToken 만료시에 새로 발급받기 위한 토큰
        return createToken(
                userId,
                null,
                null,
                jwtProperties.getRefreshTokenExpiration(),
                "refresh"
        );
    }

    private String createToken(Long userId, String email, String role, long expirationMs, String tokenType) { //토큰이 실제로 만들어지는 부분
        Instant now = Instant.now();
        JwtBuilder builder = Jwts.builder() //payload적는 메소드
            .subject(String.valueOf(userId)) //누구의 토큰인지
            .issuedAt(Date.from(now)) //생성시간
            .expiration(Date.from(now.plusMillis(expirationMs))) //만료시간 = now + expiration
            .claim("tokenType", tokenType);

        if(email != null) builder.claim("email", email);
        if(role != null) builder.claim("role", role);

        return builder.signWith(secretKey).compact(); //지금까지 만든 토큰 내용에 서명을 붙인다
        //-> JWT완성!.
    }

    public Claims getClaims(String token) { //token = JWT 문자열, 토큰을 해석하는 함수임
        return Jwts.parser() //parser가 payload를 읽어서 꺼내준다
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload(); //-> 결과는 Claims claims임 claims.getSubjet
    }

    /*get 붙은 함수들은 JWT의 Payload 부분을 읽고(getClaims를 통해),
    Payload 안에 subject나 claim으로 넣어둔 값들을 꺼내오는 함수들이다. */
    public Long getUserId(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getTokenType(String token) {
        return getClaims(token).get("tokenType", String.class);
    }


    public boolean isValid(String token) {
        try {
            getClaims(token); //payload를 읽어옴
            return true;
        } catch(JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
