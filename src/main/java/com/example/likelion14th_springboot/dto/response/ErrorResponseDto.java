package com.example.likelion14th_springboot.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponseDto {
    private final String code;
    private final String message;
}
