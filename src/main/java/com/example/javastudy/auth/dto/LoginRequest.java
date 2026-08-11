package com.example.javastudy.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * LoginRequest
 *
 * 로그인 요청(Request) DTO다.
 *
 * 이 클래스의 역할:
 * - 클라이언트가 로그인할 때 보내는 값을 받는다.
 * - 서버는 이 DTO를 통해 이메일과 비밀번호를 전달받는다.
 * - 회원가입 DTO처럼 입력값을 담는 그릇이지만,
 *   로그인 전용이라는 점이 다르다.
 *
 * 왜 DTO를 따로 쓰냐면:
 * - 회원가입과 로그인은 요구하는 값이 다르기 때문
 * - API 구조를 명확하게 나누기 위해서
 * - 검증을 요청 단위로 다르게 걸기 위해서
 */
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    /*
     * email
     *
     * 로그인할 때 사용하는 이메일이다.
     * DB에서 이 이메일로 사용자를 찾게 된다.
     *
     * @NotBlank
     * - 공백/빈값을 막는다.
     *
     * @Email
     * - 이메일 형식인지 검사한다.
     *
     * @Size(max = 100)
     * - 너무 긴 입력을 제한한다.
     */
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 아닙니다.")
    @Size(max = 254, message = "이메일은 254자 이하여야 합니다.")
    private String email;

    /*
     * password
     *
     * 로그인할 때 입력하는 비밀번호다.
     * 회원가입 때 저장된 암호화된 비밀번호와 비교할 때 사용한다.
     *
     * @NotBlank
     * - 비밀번호가 비어 있으면 안 된다.
     *
     * @Size
     * - 길이를 적당히 제한한다.
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    private String password;
}
