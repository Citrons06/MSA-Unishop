package my.payservice.pay.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.payservice.pay.entity.Pay;

@Getter @Setter
@NoArgsConstructor
public class PayResponse {

    private Long id;
    private String username;
    private int amount;
    private String payStatus;

    public PayResponse(Pay pay) {
        this.id = pay.getId();
        this.username = pay.getUsername();
        this.amount = pay.getAmount();
        this.payStatus = pay.getPayStatus().name();
    }
}
