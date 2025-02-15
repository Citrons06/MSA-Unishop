package my.payservice.adapter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class SoldTimeDto {

    private boolean isPreOrder;
    private LocalDateTime preOrderStartAt;
}
