package my.userservice.cart.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@RedisHash("cart")
public class Cart implements Serializable {

    @Id
    private String memberId;

    private List<CartItem> items = new ArrayList<>();
}