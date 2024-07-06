package my.payservice.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRequest {

    private String username;
    private String orderUsername;
    private Long itemId;
    private int quantity;
    private int amount;
    private String orderAddress;
    private String orderTel;
}
