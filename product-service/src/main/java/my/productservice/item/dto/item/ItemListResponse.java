package my.productservice.item.dto.item;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ItemListResponse {

    private List<SimpleItemDto> items;
    private PageInfo pageInfo;

    @Getter
    @Setter
    public static class SimpleItemDto {
        private Long itemId;
        private String itemName;
        private int price;
        private int quantity;
        private String mainImageUrl;
        private String itemSellStatus;
        private boolean preOrder;
    }

    @Getter
    @Setter
    public static class PageInfo {
        private int currentPage;
        private int totalPages;
        private long totalItems;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}
