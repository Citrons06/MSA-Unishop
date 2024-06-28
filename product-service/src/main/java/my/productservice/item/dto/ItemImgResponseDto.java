package my.productservice.item.dto;

import lombok.Getter;
import lombok.Setter;
import my.productservice.item.entity.ItemImg;

@Getter @Setter
public class ItemImgResponseDto {

    private Long id;
    private String imgName;
    private String imgUrl;
    private String oriImgName;
    private String repImgYn;

    public ItemImgResponseDto(ItemImg itemImg) {
        this.id = itemImg.getId();
        this.imgName = itemImg.getImgName();
        this.imgUrl = itemImg.getImgUrl();
        this.oriImgName = itemImg.getOriImgName();
        this.repImgYn = itemImg.getRepImgYn();
    }
}
