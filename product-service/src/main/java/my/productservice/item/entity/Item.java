package my.productservice.item.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import my.productservice.common.entity.BaseEntity;
import my.productservice.item.dto.item.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    private String itemName;

    private Integer price;

    private Integer itemSellCount = 0;

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ItemImg> itemImgList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private boolean isPreOrder;  // 예약 구매 상품 여부

    private LocalDateTime preOrderStartAt;  // 예약 구매 시작 시간

    public Item(ItemRequestDto itemRequestDto, Category category) {
        this.itemName = itemRequestDto.getItemName();
        this.price = itemRequestDto.getPrice();
        this.itemSellStatus = itemRequestDto.getItemSellStatus();
        this.category = category;
        this.itemSellCount = 0;
        this.isPreOrder = itemRequestDto.isPreOrder();
        this.preOrderStartAt = itemRequestDto.getPreOrderStartAt();
    }

    public void updateItem(ItemRequestDto itemRequestDto, Category category) {
        this.itemName = itemRequestDto.getItemName();
        this.price = itemRequestDto.getPrice();
        this.itemSellStatus = itemRequestDto.getItemSellStatus();
        this.category = category;
        this.isPreOrder = itemRequestDto.isPreOrder();
        this.preOrderStartAt = itemRequestDto.getPreOrderStartAt();
    }

    public void updateItemImgs(List<ItemImg> newItemImgs) {
        this.itemImgList.clear();
        this.itemImgList.addAll(newItemImgs);
    }

    public void updateItemSellCount(int quantity) {
        if (this.itemSellCount == null) {
            this.itemSellCount = 0;
        }
        this.itemSellCount += quantity;
    }
}