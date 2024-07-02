package my.productservice.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.admin.repository.CategoryRepository;
import my.productservice.admin.repository.ItemImgRepository;
import my.productservice.exception.CommonException;
import my.productservice.exception.ErrorCode;
import my.productservice.item.dto.CreateItemResponse;
import my.productservice.item.dto.ItemRequestDto;
import my.productservice.item.dto.ItemResponseDto;
import my.productservice.item.entity.Category;
import my.productservice.item.entity.Item;
import my.productservice.item.entity.ItemImg;
import my.productservice.item.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ItemAdminService {

    @Value("${itemImgLocation}")
    private String itemImgLocation;

    private final ItemRepository itemRepository;
    private final FileService fileService;
    private final ItemImgRepository itemImgRepository;
    private final CategoryRepository categoryRepository;

    public CreateItemResponse createItem(ItemRequestDto itemRequestDto) throws IOException {
        Category category = categoryRepository.findById(itemRequestDto.getCategoryId())
                .orElseThrow(() -> new CommonException(ErrorCode.CATEGORY_NOT_FOUND));

        Item item = new Item(itemRequestDto, category);
        itemRepository.save(item);
        log.info("상품 등록: " + item.getItemName());

        List<ItemImg> itemImgList = new ArrayList<>();
        for (MultipartFile itemImgFile : itemRequestDto.getItemImgFileList()) {
            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
            String imgUrl = "/images/" + imgName;

            ItemImg itemImg = new ItemImg();
            itemImg.setImgName(imgName);
            itemImg.setImgUrl(imgUrl);
            itemImg.setOriImgName(oriImgName);
            itemImg.setItem(item);

            itemImgRepository.save(itemImg);
            itemImgList.add(itemImg);
        }

        return new CreateItemResponse(item, itemRequestDto.getQuantity());
    }

    public ItemResponseDto updateItem(Long itemId, ItemRequestDto itemRequestDto, List<MultipartFile> itemImgFileList, Long categoryId) throws IOException {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CommonException(ErrorCode.CATEGORY_NOT_FOUND));
        item.updateItem(itemRequestDto, category);

        List<ItemImg> itemImgList = new ArrayList<>();
        for (MultipartFile itemImgFile : itemImgFileList) {
            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
            String imgUrl = "/images/" + imgName;

            ItemImg itemImg = new ItemImg();
            itemImg.setImgName(imgName);
            itemImg.setImgUrl(imgUrl);
            itemImg.setOriImgName(oriImgName);
            itemImg.setItem(item);

            itemImgRepository.save(itemImg);
            itemImgList.add(itemImg);
        }
        item.updateItemImgs(itemImgList);
        return new ItemResponseDto(item);
    }

    @Transactional(readOnly = true)
    public ItemResponseDto getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));
        return new ItemResponseDto(item);
    }

    public void deleteItem(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponseDto> getItems(PageRequest pageRequest) {
        Page<Item> items = itemRepository.findAllWithImagesAndCategory(pageRequest);
        return items.map(ItemResponseDto::new);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponseDto> searchItemsByName(String itemName, PageRequest pageRequest) {
        Page<Item> items = itemRepository.findByItemNameContaining(itemName, pageRequest);
        return items.map(ItemResponseDto::new);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponseDto> getItemsByCategory(Long categoryId, PageRequest pageRequest) {
        Page<Item> items = itemRepository.findByCategoryId(categoryId, pageRequest);
        return items.map(ItemResponseDto::new);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponseDto> searchItemsByCategoryAndItemName(Long categoryId, String itemName, PageRequest pageRequest) {
        Page<Item> items = itemRepository.findByCategoryIdAndItemNameContaining(categoryId, itemName, pageRequest);
        return items.map(ItemResponseDto::new);
    }
}