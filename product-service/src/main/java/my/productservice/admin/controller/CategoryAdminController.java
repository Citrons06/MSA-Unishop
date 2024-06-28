package my.productservice.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.admin.service.CategoryAdminService;
import my.productservice.item.dto.CategoryResponseDto;
import my.productservice.item.dto.CategoryRequestDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/product/admin/category")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryAdminService categoryAdminService;

    @GetMapping("/create")
    public String createCategory(Model model) {
        model.addAttribute("category", new CategoryRequestDto());
        return "admin/category/createCategoryForm";
    }

    @GetMapping("/update/{categoryId}")
    public String updateCategory(@PathVariable Long categoryId, Model model) {
        CategoryResponseDto categoryResponseDto = categoryAdminService.getCategory(categoryId);
        model.addAttribute("category", categoryResponseDto);
        return "admin/category/updateCategoryForm";
    }

    @GetMapping("/list")
    public String categoryList(Model model) {
        model.addAttribute("categories", categoryAdminService.getCategories());
        return "admin/category/categoryList";
    }

    @PostMapping("/create")
    public String createCategory(CategoryRequestDto categoryRequestDto) {
        categoryAdminService.createCategory(categoryRequestDto);
        return "redirect:/admin/category/list";
    }

    @PutMapping("/update/{categoryId}")
    public String updateCategory(@PathVariable Long categoryId, CategoryResponseDto categoryResponseDto) {
        categoryAdminService.updateCategory(categoryId, categoryResponseDto);
        return "redirect:/admin/category/list";
    }

    @DeleteMapping("/delete/{categoryId}")
    public String deleteCategory(@PathVariable Long categoryId) {
        categoryAdminService.deleteCategory(categoryId);
        return "redirect:/admin/category/list";
    }
}
