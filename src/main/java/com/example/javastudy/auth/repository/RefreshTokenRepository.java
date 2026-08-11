package com.example.javastudy.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.javastudy.auth.entity.RefreshToken;

//리포지토리는 인터페이스를 구현하는 부분임
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
//<RefreshToken, Long>에서 RefreshToken은 엔티티, Long은 @Id 타입

    //기본 메서드에서는 없는데 사용할 것들
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUserId(Long userId); //같은 userId를 가진 모든 리프레시토큰 조회
    void deleteByToken(String token); //특정 refresh token삭제 -> 이떄 특정 토큰은 기기 하나를 의미
    void deleteAllByUserId(Long userId); //로그아웃등 아에 초기로 돌림
}
