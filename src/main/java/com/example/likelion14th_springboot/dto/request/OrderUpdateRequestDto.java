package com.example.likelion14th_springboot.dto.request;

import lombok.Getter;

@Getter
public class OrderUpdateRequestDto {
    private String receiverName;
    private String receiverPhone;
    private String roadAddress;
    private String detailAddress;
    private String zipCode;
}
