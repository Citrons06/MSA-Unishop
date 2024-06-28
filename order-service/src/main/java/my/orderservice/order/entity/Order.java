package my.orderservice.order.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.orderservice.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity(name = "orders")
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    private LocalDateTime orderDate = LocalDateTime.now();
    private LocalDateTime updatedDate;

    @Setter
    private Integer orderPrice;

    @Setter
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private Long memberId;

    private String orderUsername;

    private String orderAddress;

    private String orderTel;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDateTime returnRequestDate; // 반품 요청일

    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }

    @Builder
    public Order(Long memberId, String orderUsername, String orderAddress, String orderTel, OrderStatus orderStatus, Integer orderPrice) {
        this.memberId = memberId;
        this.orderUsername = orderUsername;
        this.orderAddress = orderAddress;
        this.orderTel = orderTel;
        this.orderStatus = orderStatus;
        this.orderPrice = orderPrice;
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCEL;
    }

    public void returnOrder() {
        if (orderStatus != OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송 완료된 상품만 반품이 가능합니다.");
        }

        if (LocalDateTime.now().isAfter(orderDate.plusDays(3))) {
            throw new IllegalStateException("배송 완료 후 1일 이내에만 반품이 가능합니다.");
        }

        this.orderStatus = OrderStatus.RETURN_REQUESTED;
        this.returnRequestDate = LocalDateTime.now();
    }

    public void completeReturn() {
        if (orderStatus != OrderStatus.RETURN_REQUESTED) {
            throw new IllegalStateException("반품 요청 상태인 주문만 반품 완료 처리가 가능합니다.");
        }

        this.orderStatus = OrderStatus.RETURN_COMPLETE;
    }

    public void completeDelivery() {
        this.orderStatus = OrderStatus.DELIVERED;
    }

    // 주문의 총 금액 계산
    public int getTotalPrice() {
        return orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }
}