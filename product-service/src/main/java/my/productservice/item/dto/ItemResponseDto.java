package my.productservice.item.dto;

import lombok.Getter;
import lombok.Setter;
import my.productservice.item.entity.Item;

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
    }
}