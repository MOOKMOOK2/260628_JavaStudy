package com.example.javastudy.user.entity;

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

/*
 * User 엔티티(Entity) - DB 테이블과 연결되는 자바 객체
 * MySQL의 테이블/행을 자바 객체처럼 다루게 해준다.
 *
 * 이 클래스는 회원 정보를 DB에 저장하기 위한 저장용 모델이다.
 *
 * 쉽게 말하면:
 * - 사용자가 회원가입할 때 입력한 정보
 * - 그걸 자바 객체 형태로 받아서
 * - DB의 users 테이블에 저장하는 역할을 한다.
 *
 * 중요한 점:
 * - DTO는 요청/응답용
 * - Entity는 DB 저장용
 * - 이 클래스는 API에서 바로 내려주는 데이터가 아니라
 *   DB와 직접 연결되는 데이터 구조다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //JPA가 엔티티를 만들때 필요함
public class User {

     /* @GeneratedValue(strategy = GenerationType.IDENTITY)
     * - DB가 자동으로 숫자를 증가시켜서 id를 만들어준다.
     * - 즉, 저장할 때마다 1, 2, 3... 식으로 자동 증가한다.*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;


     //Service에서 PasswordEncoder로 암호화해서 넣을 예정이다.
    @Column(nullable = false, length = 255)
    private String password;


    @Column(nullable = false, length = 50)
    private String nickname;

    /* role
     * 사용자의 권한을 나타낸다.
     * 예:
     * - ROLE_USER
     * - ROLE_ADMI*/
    @Column(nullable = false, length = 30)
    private String role;

    //회원가입 한 시각
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //마지막 수정시각
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public User(String email, String password, String nickname, String role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    @PrePersist //@PrePersist = Entity가 DB에 처음 INSERT 되기 직전에 자동 실행되는 메서드에 붙이는 어노테이션
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