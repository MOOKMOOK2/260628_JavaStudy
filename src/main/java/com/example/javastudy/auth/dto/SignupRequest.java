package com.example.javastudy.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * SignupRequest
 *
 * 회원가입 요청(Request) DTO다.
 *
 * 이 클래스의 역할:
 * - 클라이언트가 회원가입할 때 보내는 값들을 받는다.
 * - 서버는 이 DTO를 통해 이메일, 비밀번호, 닉네임을 전달받는다.
 * - Entity처럼 DB에 바로 저장하는 용도가 아니라,
 *   "사용자가 보낸 입력값"을 담는 그릇이다.
 *
 * 왜 DTO를 쓰냐면:
 * - 요청 데이터와 DB 저장 모델(Entity)을 분리하기 위해서
 * - 검증(@Valid, validation annotation)을 붙이기 쉽기 위해서
 * - API 입출력 구조를 명확하게 하기 위해서
 */
@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {

    /*
     * email
     *
     * 회원가입에 사용할 이메일 주소다.
     * 로그인 아이디 역할도 겸할 가능성이 높다.
     *
     * @NotBlank
     * - 빈 문자열, 공백만 있는 값은 허용하지 않는다.
     *
     * @Email
     * - 이메일 형식인지 검사한다.
     *
     * @Size(max = 100)
     * - 너무 긴 입력을 막는다.
     */
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 아닙니다.")
    @Size(max = 254, message = "이메일은 254자 이하여야 합니다.")
    private String email;

    /*
     * password
     *
     * 사용자가 입력하는 비밀번호다.
     * 이 값은 그대로 DB에 저장하지 않고,
     * Service에서 PasswordEncoder로 암호화해서 저장한다.
     *
     * @NotBlank
     * - 비밀번호는 비어 있으면 안 된다.
     *
     * @Size
     * - 최소/최대 길이를 제한한다.
     * - 너무 짧거나 너무 긴 비밀번호를 막는다.
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    private String password;

    /*
     * nickname
     *
     * 회원가입 후 화면에 보여줄 닉네임이다.
     * 이메일 대신 사용자를 식별하기 쉬운 이름이다.
     *
     * @NotBlank
     * - 닉네임은 비어 있으면 안 된다.
     *
     * @Size
     * - 너무 짧거나 너무 긴 닉네임을 막는다.
     */
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    private String nickname;
}
