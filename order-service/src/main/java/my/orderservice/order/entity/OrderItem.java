package my.orderservice.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.orderservice.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor
public class OrderItem extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @Setter
    private String itemName;

    @Setter
    private Integer orderPrice;
    private Integer count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Setter
    private Long itemId;

    public OrderItem(Order order, Long id, String itemName, int quantity, int price) {
        this.order = order;
        this.itemId = id;
        this.itemName = itemName;
        this.count = quantity;
        this.orderPrice = price;
    }


    public int getTotalPrice() {
        return orderPrice * count;
    }
}
