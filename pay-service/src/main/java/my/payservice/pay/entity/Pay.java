package my.payservice.pay.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor
public class Pay extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "pay_id")
    private Long id;

    private String username;

    @Setter
    private int amount;

    @Setter
    @Enumerated(EnumType.STRING)
    private PayStatus payStatus;

    @Builder
    public Pay(Long id, String username, int amount, PayStatus payStatus) {
        this.id = id;
        this.username = username;
        this.amount = amount;
        this.payStatus = payStatus;
    }
}
