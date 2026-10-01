package com.example.likelion14th_springboot.service;

import com.example.likelion14th_springboot.domain.Member;
import com.example.likelion14th_springboot.domain.Orders;
import com.example.likelion14th_springboot.domain.Product;
import com.example.likelion14th_springboot.domain.ShippingAddress;
import com.example.likelion14th_springboot.domain.mapping.ProductOrders;
import com.example.likelion14th_springboot.dto.request.OrderCreateRequestDto;
import com.example.likelion14th_springboot.dto.request.OrderUpdateRequestDto;
import com.example.likelion14th_springboot.dto.response.OrderResponseDto;
import com.example.likelion14th_springboot.enums.DeliverStatus;
import com.example.likelion14th_springboot.repository.MemberRepository;
import com.example.likelion14th_springboot.repository.OrderRepository;
import com.example.likelion14th_springboot.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderCreateRequestDto dto) {
        // 1. 구매자 조회
        Member buyer = memberRepository.findById(dto.getBuyerId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 상품 조회
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // 3. 재고 확인
        if (product.getStock() < dto.getQuantity()) {
            throw new IllegalArgumentException("상품 재고가 부족합니다.");
        }

        // 4. 잔액 확인
        int totalPrice = product.getPrice() * dto.getQuantity();
        if (buyer.getDeposit() < totalPrice) {
            throw new IllegalArgumentException("계좌 잔액이 부족합니다.");
        }

        // 5. 배송정보 생성
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .receiverName(dto.getReceiverName())
                .receiverPhone(dto.getReceiverPhone())
                .roadAddress(dto.getRoadAddress())
                .detailAddress(dto.getDetailAddress())
                .zipCode(dto.getZipCode())
                .build();

        // 6. 주문 생성
        Orders order = Orders.builder()
                .buyer(buyer)
                .deliverStatus(DeliverStatus.PREPARATION)
                .shippingAddress(shippingAddress)
                .build();
        Orders savedOrder = orderRepository.save(order);

        // 7. 주문-상품 연결 (중간테이블)
        ProductOrders productOrders = ProductOrders.builder()
                .product(product)
                .orders(savedOrder)
                .quantity(dto.getQuantity())
                .build();
        // ProductOrders는 별도 Repository 없이 cascade로 저장되게 하려면
        // Orders 쪽 리스트에 추가하는 방식도 가능하지만, 여기서는 간단히 아래처럼 처리

        // 8. 재고 차감 & 잔액 차감
        product.reduceStock(dto.getQuantity());
        buyer.useDeposit(totalPrice);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    // 구매자별 주문 목록 조회
    public List<OrderResponseDto> getOrdersByBuyer(Long buyerId) {
        return orderRepository.findByBuyerId(buyerId).stream()
                .map(OrderResponseDto::fromEntity)
                .toList();
    }

    // 단건 주문 조회
    public OrderResponseDto getOrderById(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));
        return OrderResponseDto.fromEntity(order);
    }

    // 배송정보 수정 (PREPARATION 상태일 때만)
    @Transactional
    public OrderResponseDto updateShippingAddress(Long orderId, OrderUpdateRequestDto dto) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));

        if (order.getDeliverStatus() != DeliverStatus.PREPARATION) {
            throw new IllegalArgumentException("배송 준비 중인 주문만 배송정보를 수정할 수 있습니다.");
        }

        ShippingAddress newAddress = ShippingAddress.builder()
                .receiverName(dto.getReceiverName())
                .receiverPhone(dto.getReceiverPhone())
                .roadAddress(dto.getRoadAddress())
                .detailAddress(dto.getDetailAddress())
                .zipCode(dto.getZipCode())
                .build();

        // Orders에 shippingAddress를 바꿔주는 메서드가 없으므로 아래 update 메서드를 Orders에 추가해야 함
        order.updateShippingAddress(newAddress);

        return OrderResponseDto.fromEntity(order);
    }

    // 주문 삭제 (Soft Delete, COMPLETED 상태일 때만)
    @Transactional
    public void deleteOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));

        if (order.getDeliverStatus() != DeliverStatus.COMPLETED) {
            throw new IllegalArgumentException("배송 완료된 주문만 삭제할 수 있습니다.");
        }

        order.softDelete();
    }
}
