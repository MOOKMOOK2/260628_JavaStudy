package com.example.javastudy.global.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("리프레시 토큰이 유효하지 않습니다.");
    }

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
