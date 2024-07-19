package my.productservice.item.dto.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.productservice.item.entity.Category;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class CategoryResponseDto {

    private Long id;
    private String categoryName;

    public CategoryResponseDto(Category category) {
        this.id = category.getId();
        this.categoryName = category.getCategoryName();
    }

    public static List<CategoryResponseDto> listFromItems(List<Category> categories) {
        return categories.stream().map(CategoryResponseDto::new).toList();
    }
}
