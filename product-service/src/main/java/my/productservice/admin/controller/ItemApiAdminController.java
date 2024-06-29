package my.productservice.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.admin.service.ItemAdminService;
import my.productservice.item.dto.ItemRequestDto;
import my.productservice.item.dto.ItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/product/admin")
@RequiredArgsConstructor
public class ItemApiAdminController {

    private final ItemAdminService itemAdminService;

    @PostMapping("/create")
    public ResponseEntity<?> createItem(@ModelAttribute ItemRequestDto itemRequestDto,
                                        @RequestPart("itemImgFileList") List<MultipartFile> itemImgFileList,
                                        @RequestParam("categoryId") Long categoryId) {
        try {
            itemRequestDto.setItemImgFileList(itemImgFileList);
            itemRequestDto.setCategoryId(categoryId);
            ItemResponseDto item = itemAdminService.createItem(itemRequestDto);
            return ResponseEntity.ok().body(item);
        } catch (IOException e) {
            log.error("Item creation failed", e);
            return ResponseEntity.status(500).body("Item creation failed");
        }
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<?> getItem(@PathVariable Long itemId) {
        ItemResponseDto item = itemAdminService.getItem(itemId);
        return ResponseEntity.ok().body(item);
    }

    @PutMapping("/update/{itemId}")
    public ResponseEntity<?> updateItem(@PathVariable("itemId") Long itemId,
                                        @ModelAttribute ItemRequestDto itemRequestDto,
                                        @RequestParam("itemImgFileList") List<MultipartFile> itemImgFileList,
                                        @RequestParam("categoryId") Long categoryId) {
        try {
            ItemResponseDto item = itemAdminService.updateItem(itemId, itemRequestDto, itemImgFileList, categoryId);
            return ResponseEntity.ok().body(item);
        } catch (IOException e) {
            log.error("Item update failed", e);
            return ResponseEntity.status(500).body("Item update failed");
        }
    }

    @DeleteMapping("/delete/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId) {
        itemAdminService.deleteItem(itemId);
        return ResponseEntity.ok().body("Item deleted");
    }

    @GetMapping("/list")
    public ResponseEntity<?> getItems(@RequestParam(value = "page", defaultValue = "0") int page,
                                      @RequestParam(value = "size", defaultValue = "8") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ItemResponseDto> items = itemAdminService.getItems(pageRequest);
        return ResponseEntity.ok().body(items);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchItems(@RequestParam(value = "search") String search,
                                         @RequestParam(value = "category", required = false) Long categoryId,
                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "8") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ItemResponseDto> items;
        if (categoryId != null) {
            items = itemAdminService.searchItemsByCategoryAndItemName(categoryId, search, pageRequest);
        } else {
            items = itemAdminService.searchItemsByName(search, pageRequest);
        }
        return ResponseEntity.ok().body(items);
    }
}