package com.example.likelion14th_springboot.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    DUPLICATE_NAME(HttpStatus.CONFLICT, "이미 존재하는 이름입니다.");

    private final HttpStatus status;
    private final String message;
}