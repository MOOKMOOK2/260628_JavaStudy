# Development Notes

## 프로젝트

Spring Boot + MySQL 기반 스터디 매칭 플랫폼

## 기준일

2026-08-22

## 현재 완료 상태

### 프로젝트 기본 설정

- Java 21, Spring Boot 3.5.x, Gradle 구성
- Spring Web, JPA, Security, Validation, Thymeleaf, MySQL Driver, Lombok 구성
- `application.properties`에 MySQL과 JWT 설정 구조 존재
- DB 연결 대상은 `user_info`, 계정은 `study_app`
- DB 비밀번호는 `DB_PASSWORD` 환경변수로 받는 구조
- JWT Secret은 `JWT_SECRET` 환경변수로 덮어쓸 수 있는 구조

### User·Auth 도메인

- `User` Entity 구현
- `UserRepository` 구현
- 회원가입 DTO와 Validation 구현
- 회원가입 Service 구현
- 이메일 중복 확인 구현
- BCrypt 비밀번호 암호화 구현
- 로그인 DTO와 응답 DTO 구현
- 로그인 성공 시 Access Token과 Refresh Token 발급
- Refresh Token Entity와 Repository 구현
- Refresh Token DB 저장 구현
- 쿠키 기반 Access Token 인증 필터 구현
- JWT Access Token 검증과 SecurityContext 인증 등록 구현
- Access Token 재발급 구현
- 로그아웃과 Refresh Token 삭제 구현
- 로그인·회원가입 Thymeleaf 화면 구현
- 로그인 실패 메시지와 회원가입 Validation 메시지 처리 구현
- 인증 관련 커스텀 예외와 전역 예외 처리 일부 구현

`auth` 도메인의 1차 구현은 완료된 상태다. 이후 테스트와 보안 보완은 별도 개선 작업으로 진행한다.

## 현재 작업 위치

`study` 도메인 개발 시작 단계

현재 파일 상태:

- `study/entity/Study.java`: Entity 뼈대와 `id`만 존재
- `study/repository/StudyRepository.java`: 아직 Repository 구현 전
- `study/service/StudyService.java`: 아직 Service 구현 전
- `study/controller/StudyController.java`: 아직 Controller 구현 전
- `study/dto`: 아직 생성되지 않음

## 바로 다음 작업

`Study Entity` 설계 및 구현

먼저 정할 필드:

- `id`
- `title`
- `content`
- `maxMembers`
- `region`
- 온라인/오프라인 구분
- 모집 상태
- 작성자
- `createdAt`
- `updatedAt`

상태값과 진행 방식은 문자열보다 enum 사용을 우선 검토한다.

## Study 개발 순서

```text
1. Study Entity
2. StudyRepository
3. StudyCreateRequest / StudyUpdateRequest
4. StudyResponse
5. StudyService
6. StudyController
7. JWT 사용자와 작성자 연결
8. 작성자 수정·삭제 권한 확인
9. Study 관련 예외 처리
10. CRUD 테스트
```

## 예정 API

```text
POST   /studies
GET    /studies
GET    /studies/{id}
PATCH  /studies/{id}
DELETE /studies/{id}
```

권한 기준:

- 목록·상세 조회: 공개 여부를 구현 단계에서 결정
- 생성: 로그인 필요
- 수정: 작성자 본인만 가능
- 삭제: 작성자 본인만 가능

## DB 운영 방식

- Mac과 Windows에 MySQL DB를 각각 둔다.
- 두 DB는 실제 데이터가 서로 동기화되지 않는다.
- GitHub에는 Java 코드와 개발 문서만 동기화한다.
- 두 환경의 DB 이름과 계정명을 동일하게 유지한다.
- 로컬 DB 비밀번호와 JWT Secret은 각 컴퓨터 환경변수로 관리한다.
- Entity 변경으로 개발 DB 스키마를 맞추는 동안 `ddl-auto=update`를 사용한다.
- 테이블 변경 이력이 중요해지는 시점에 Flyway 등 마이그레이션 도입을 검토한다.

## 확인된 주의사항

- `src/main/resources/application.properties`는 현재 `.gitignore`에 포함되어 있어 GitHub에 올라가지 않는다.
- 다른 컴퓨터에서 실행하려면 DB 설정 방법을 별도로 전달해야 한다.
- `application-example.properties`를 Git에 저장하는 방식을 검토한다.
- `StudyRepository`는 현재 일반 class이므로 `JpaRepository<Study, Long>` 기반 interface로 구현해야 한다.
- `StudyService`와 `StudyController`는 현재 Spring Bean 어노테이션과 실제 로직이 없다.
- 현재 Security 설정은 `/auth/**`만 공개하고 나머지는 인증 필요다. Study 조회 공개 여부를 정하면 Security 경로를 조정해야 한다.
- 로그인 성공 후 `/`로 이동하지만 메인 페이지 Controller 존재 여부를 확인해야 한다.
- 테스트는 기본 Context 테스트만 있으므로 Auth와 Study 기능 테스트가 필요하다.
- `.DS_Store`가 저장소에 포함되어 있으므로 `.gitignore` 정리 대상이다.

## 다음 Codex 인수인계 문장

`AGENTS.md`와 `DEV_NOTES.md`를 읽고, 현재 완료된 auth 도메인은 건드리지 말고 Study Entity 설계부터 한 단계씩 진행해줘. 먼저 Study에 필요한 필드와 각 필드가 필요한 이유를 설명하고, 파일 수정은 내가 허용한 뒤에 진행해줘.
