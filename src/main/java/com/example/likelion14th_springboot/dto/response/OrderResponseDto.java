package com.example.likelion14th_springboot.dto.response;

import com.example.likelion14th_springboot.domain.Orders;
import com.example.likelion14th_springboot.enums.DeliverStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponseDto {
    private Long id;
    private DeliverStatus deliverStatus;
    private String receiverName;
    private String receiverPhone;
    private String roadAddress;
    private String detailAddress;
    private String zipCode;

    public static OrderResponseDto fromEntity(Orders orders) {
        return OrderResponseDto.builder()
                .id(orders.getId())
                .deliverStatus(orders.getDeliverStatus())
                .receiverName(orders.getShippingAddress().getReceiverName())
                .receiverPhone(orders.getShippingAddress().getReceiverPhone())
                .roadAddress(orders.getShippingAddress().getRoadAddress())
                .detailAddress(orders.getShippingAddress().getDetailAddress())
                .zipCode(orders.getShippingAddress().getZipCode())
                .build();
    }
}
