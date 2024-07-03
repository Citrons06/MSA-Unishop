package my.productservice.inventory.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class UpdateQuantityRequest {

    private int quantity;

    public UpdateQuantityRequest(int quantity) {
        this.quantity = quantity;
    }
}
