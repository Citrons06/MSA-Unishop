package my.productservice.item.dto.item;

import lombok.Getter;
import lombok.Setter;
import my.productservice.item.entity.Item;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
public class ItemResponseDto {

    private Long itemId;
    private String itemName;
    private int price;
    private int quantity;
    private boolean hasImages;
    private String itemSellStatus;
    private boolean isPreOrder;  // 예약 구매 상품 여부

    private LocalDateTime preOrderStartAt;  // 예약 구매 시작 시간

    private List<ItemImgResponseDto> itemImgList = new ArrayList<>();

    public ItemResponseDto(Item item, int quantity) {
        this.itemId = item.getId();
        this.itemName = item.getItemName();
        this.price = item.getPrice();
        this.quantity = quantity;
        this.itemSellStatus = item.getItemSellStatus().name();
        this.itemImgList = item.getItemImgList().stream()
                .map(ItemImgResponseDto::new)
                .collect(Collectors.toList());
        this.hasImages = !this.itemImgList.isEmpty();
        this.isPreOrder = item.isPreOrder();
        this.preOrderStartAt = item.getPreOrderStartAt();
    }

    public ItemResponseDto(Item item) {
        this.itemId = item.getId();
        this.itemName = item.getItemName();
        this.price = item.getPrice();
        this.itemSellStatus = item.getItemSellStatus().name();
        this.itemImgList = item.getItemImgList().stream()
                .map(ItemImgResponseDto::new)
                .collect(Collectors.toList());
        this.hasImages = !this.itemImgList.isEmpty();
        this.isPreOrder = item.isPreOrder();
        this.preOrderStartAt = item.getPreOrderStartAt();
    }

    public ItemResponseDto(Long itemId, String itemName, int price, int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}