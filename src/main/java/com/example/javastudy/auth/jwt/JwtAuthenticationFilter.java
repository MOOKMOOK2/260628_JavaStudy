package com.example.javastudy.auth.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override //extends 했으니까 재정의
    protected void doFilterInternal( //부모에 있는 함수, 요청이 올때마다 이 함수를 실행
        HttpServletRequest request, //브라우저 -> 서버 요청 정보(쿠키가 있고 그 안에 access token이 있음)
        HttpServletResponse response, //서버 -> 브라우저 응답 객체
        FilterChain filterChain //이 필터가 다음 필터 혹은 컨트롤러로 이어지게 해줌
    ) throws ServletException, IOException { //여기에 실제 액세스 토큰 검사 로직이 들어간다.
        // 1. 쿠키에서 accessToken 꺼내기
        String accessToken = getAccessTokenFromCookie(request); //값만 들어가있음
        // 2. 토큰 유효성 검사하기 -> 토큰 자체가 값이 없는경우, 토큰의 payload가 유효하지 않은경우, payload의 role이 access가 아닌경우
        if(accessToken==null || accessToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        if(!jwtTokenProvider.isValid(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }
        if(!"access".equals(jwtTokenProvider.getTokenType(accessToken))) {
            filterChain.doFilter(request, response);
            return;
        }
        // 3. 유효하면 사용자 정보 꺼내기
        Long userId = jwtTokenProvider.getUserId(accessToken);
        String role = jwtTokenProvider.getRole(accessToken);
        if(userId==null || role==null || role.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        // 4. SecurityContext에 인증 정보 넣기
        if(SecurityContextHolder.getContext().getAuthentication() == null) {
            //JWT 정보를 꺼내서 변환하여 Spring Security 인증객체를 만들기
            List<SimpleGrantedAuthority> authorities = 
                List.of(new SimpleGrantedAuthority(role));
            //<> 타입만 넣는 리스트임, 각 role에 대한 권한 객체를 넣는 리스트, List.of로 고정크기 리스트를 만든다
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
            //인증객체의 타입은 통상 UsernamePasswordAuthenticationToken 이걸 쓴다.
            authentication.setDetails( //관례적으로 detail을 넣는대유
                new WebAuthenticationDetailsSource().buildDetails(request)
                //현재 http요청에서 부가정보를 꺼낸다 -> 접속 IP 주소, 세션 IP
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 5. 다음 필터로 넘기기
        filterChain.doFilter(request, response); //다음에 연결된 곳으로 보내주세요 ~
    }

    //요청으로 들어오는 쿠키들 중에서 AccessToken을 분리하기
    private String getAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
