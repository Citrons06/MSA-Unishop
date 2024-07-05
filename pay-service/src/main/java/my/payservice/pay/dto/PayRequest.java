package my.payservice.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayRequest {

    private String username;
    private Long itemId;
    private int quantity;
    private int amount;
}
