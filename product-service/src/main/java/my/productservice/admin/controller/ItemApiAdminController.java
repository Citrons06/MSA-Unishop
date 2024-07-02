package my.productservice.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.admin.service.ItemAdminService;
import my.productservice.exception.CommonException;
import my.productservice.exception.ErrorCode;
import my.productservice.item.dto.ItemRequestDto;
import my.productservice.item.dto.ItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/product/admin")
@RequiredArgsConstructor
public class ItemApiAdminController {

    private final ItemAdminService itemAdminService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<?> getItem(@PathVariable("itemId") Long itemId) {
        try {
            ItemResponseDto item = itemAdminService.getItem(itemId);
            return ResponseEntity.ok().body(item);
        } catch (Exception e) {
            log.error("Item not found", e);
            throw new CommonException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    @PutMapping(value = "/update/{itemId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateItem(@PathVariable("itemId") Long itemId,
                                        @ModelAttribute ItemRequestDto itemRequestDto,
                                        @RequestPart("itemImgFileList") List<MultipartFile> itemImgFileList,
                                        @RequestParam("categoryId") Long categoryId) {
        try {
            ItemResponseDto item = itemAdminService.updateItem(itemId, itemRequestDto, itemImgFileList, categoryId);
            return ResponseEntity.ok().body(item);
        } catch (IOException e) {
            log.error("Item update failed", e);
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable("itemId") Long itemId) {
        try {
            itemAdminService.deleteItem(itemId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "delete success");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            log.error("Item deletion failed", e);
            throw new CommonException(ErrorCode.PRODUCT_NOT_FOUND);
        }
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
