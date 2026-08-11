package com.example.javastudy.user.repository;

import java.util.Optional;

import com.example.javastudy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * UserRepository
 *
 * 이 인터페이스는 User 엔티티를 DB에 저장하고 조회하는 역할을 한다.
 *
 * Repository는 직접 SQL을 다 쓰는 곳이 아니라,
 * Spring Data JPA가 제공하는 기본 기능과
 * 메서드 이름 규칙으로 자동 생성되는 쿼리를 사용해서
 * DB 작업을 처리하는 계층이다.
 *
 * 중요한 점:
 * - Repository는 보통 class가 아니라 interface로 만든다.
 * - 우리가 구현 코드를 직접 쓰지 않아도 된다.
 * - Spring Data JPA가 실행 시점에 구현체를 자동으로 만들어준다.
 *
 * 이 Repository에서 얻는 것:
 * - 기본 CRUD 기능
 * - 메서드 이름 기반 자동 쿼리 생성 기능
 *
 * 예:
 * - save() -> 저장
 * - findById() -> PK로 조회
 * - findByEmail() -> email 필드로 조회
 * - existsByEmail() -> email 존재 여부 확인
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /*
     * email로 사용자 존재 여부를 확인한다.
     *
     * 왜 필요한가:
     * - 회원가입할 때 같은 이메일이 이미 있는지 확인하려고
     *
     * 어떻게 동작하나:
     * - 메서드 이름의 existsByEmail을 보고
     * - Spring Data JPA가 email 필드를 기준으로 자동 쿼리를 만든다.
     *
     * 반환값:
     * - true: 같은 이메일이 이미 존재함
     * - false: 같은 이메일이 없음
     */
    boolean existsByEmail(String email);

    /*
     * email로 사용자를 찾는다.
     *
     * 왜 필요한가:
     * - 로그인할 때 이메일로 사용자를 조회하려고
     *
     * 어떻게 동작하나:
     * - 메서드 이름의 findByEmail을 보고
     * - Spring Data JPA가 email 필드를 기준으로 자동 쿼리를 만든다.
     *
     * 반환값:
     * - Optional<User>
     * - 사용자가 있으면 User를 감싸서 반환
     * - 없으면 비어 있는 Optional 반환
     *
     * Optional을 쓰는 이유:
     * - null을 직접 다루는 것보다 안전하기 때문
     */
    Optional<User> findByEmail(String email); //있으면 유저객체 반환, 없으면 optional.empty()
    //optional : 조회가 필요할때 / boolean : 단순 존재 확인
}