package my.productservice.item.dto.item;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.productservice.item.entity.ItemSellStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class ItemRequestDto {

    @NotNull
    private String itemName;

    @NotNull
    private Integer price;

    private Integer quantity;

    private Integer item_sell_count;

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus;

    private List<MultipartFile> itemImgFileList = new ArrayList<>();

    private Long categoryId;

    private boolean isPreOrder;  // 예약 구매 상품 여부

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime preOrderStartAt;  // 예약 구매 시작 시간
}
