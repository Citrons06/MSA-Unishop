package my.payservice.pay.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class PayResponse {

    private String username;
    private Long itemId;
    private int amount;
    private String payStatus;
}
