package my.productservice.item.dto.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.productservice.item.entity.Category;

@Getter @Setter
@NoArgsConstructor
public class CategoryRequestDto {

    private String categoryName;

    public CategoryRequestDto(String categoryName) {
        this.categoryName = categoryName;
    }

    public Category toEntity() {
        return new Category(this);
    }
}
