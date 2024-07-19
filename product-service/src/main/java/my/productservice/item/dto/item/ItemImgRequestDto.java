package my.productservice.item.dto.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.productservice.item.entity.Item;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
@NoArgsConstructor
public class ItemImgRequestDto {

    private String imgName;
    private String imgUrl;
    private String oriImgName;
    private String repImgYn;
    private Item item;
    private MultipartFile imgFile;

    public ItemImgRequestDto(String imgName, String imgUrl, String oriImgName, String repImgYn, Item item, MultipartFile imgFile) {
        this.imgName = imgName;
        this.imgUrl = imgUrl;
        this.oriImgName = oriImgName;
        this.repImgYn = repImgYn;
        this.item = item;
        this.imgFile = imgFile;
    }
}
