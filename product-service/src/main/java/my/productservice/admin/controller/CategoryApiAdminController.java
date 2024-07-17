package my.productservice.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.admin.service.CategoryAdminService;
import my.productservice.item.dto.CategoryResponseDto;
import my.productservice.item.dto.CategoryRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/product/api/admin/category")
@RequiredArgsConstructor
public class CategoryApiAdminController {

    private final CategoryAdminService categoryAdminService;

    @PostMapping("/create")
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequestDto categoryRequestDto) {
        CategoryResponseDto categoryResponseDto = categoryAdminService.createCategory(categoryRequestDto);
        return ResponseEntity.ok().body(categoryResponseDto);
    }

    @PutMapping("/update/{categoryId}")
    public ResponseEntity<?> updateCategory(@PathVariable("categoryId") Long categoryId,
                                            @RequestBody CategoryResponseDto categoryResponseDto) {
        CategoryResponseDto categoryResponseDto1 = categoryAdminService.updateCategory(categoryId, categoryResponseDto);
        return ResponseEntity.ok().body(categoryResponseDto1);
    }

    @DeleteMapping("/delete/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        categoryAdminService.deleteCategory(categoryId);
        return ResponseEntity.ok().body("{ \"message\": \"delete success\" }");
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> getCategory(@PathVariable Long categoryId) {
        CategoryResponseDto categoryResponseDto = categoryAdminService.getCategory(categoryId);
        return ResponseEntity.ok().body(categoryResponseDto);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponseDto>> getCategories() {
        List<CategoryResponseDto> categories = categoryAdminService.getCategories();
        return ResponseEntity.ok().body(categories);
    }
}
