package com.example.javastudy.auth.entity; //Refresh Token 데이터를 어떤 형태로 DB에 저장할지 정의하는 파일
//entity 파일이고, entity는 JPA가 보고 DB테이블을 만들 수 있도록 함

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //기본생성자 만듦(JPA가 사용) public으로 안만들도록 제한
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //id를 DB가 자동으로 생성하도록
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 1000)
    private String token; //실제 Refresh Token JWT 문자열

    @Column(nullable = false)
    private LocalDateTime expiresAt; //Refresh Token 만료 시각

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; //DB에 처음 저장된 시각

    @Column(nullable = false)
    private LocalDateTime updatedAt; //마지막으로 토큰 정보가 변경된 시각

    //생성자(로그인 성공 후 RefreshToken 생성할때)
    public RefreshToken(Long userId, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public void updateToken(String token, LocalDateTime expiresAt) { //이미 만들어진 객체 사용
        this.token = token;
        this.expiresAt = expiresAt;
    }

    @PrePersist //DB저장되는 시점에 자동으로 
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
