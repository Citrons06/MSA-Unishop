package my.productservice.item.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import my.productservice.item.dto.CategoryRequestDto;
import my.productservice.item.dto.CategoryResponseDto;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Entity(name = "categories")
@NoArgsConstructor
public class Category {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String categoryName;

    @OneToMany(mappedBy = "category")
    private List<Item> items = new ArrayList<>();

    public Category(CategoryRequestDto categoryRequestDto) {
        this.categoryName = categoryRequestDto.getCategoryName();
    }

    public void update(CategoryResponseDto categoryResponseDto) {
        this.categoryName = categoryResponseDto.getCategoryName();
    }
}
