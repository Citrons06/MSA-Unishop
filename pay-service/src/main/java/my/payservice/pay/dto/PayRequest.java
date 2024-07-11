package my.payservice.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private Long itemId;
    private int quantity;
    private int amount;
}
