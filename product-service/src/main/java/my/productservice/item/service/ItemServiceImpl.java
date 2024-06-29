package my.productservice.item.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.item.entity.Item;
import my.productservice.item.repository.ItemRepository;
import my.productservice.item.dto.ItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public Page<ItemResponseDto> getItems(PageRequest pageRequest) {
        Page<Item> items = itemRepository.findAll(pageRequest);
        return items.map(ItemResponseDto::new);
    }

    @Override
    public Page<ItemResponseDto> searchItemsByName(String itemName, PageRequest pageRequest) {
        Page<Item> items = itemRepository.findByItemNameContaining(itemName, pageRequest);
        return items.map(ItemResponseDto::new);
    }

    @Override
    public Page<ItemResponseDto> getItemsByCategory(Long categoryId, PageRequest pageRequest) {
        Page<Item> items = itemRepository.findByCategoryId(categoryId, pageRequest);
        return items.map(ItemResponseDto::new);
    }

    @Override
    public Page<ItemResponseDto> searchItemsByCategoryAndItemName(Long categoryId, String itemName, PageRequest pageRequest) {
        Page<Item> items = itemRepository.findByCategoryIdAndItemNameContaining(categoryId, itemName, pageRequest);
        return items.map(ItemResponseDto::new);
    }

    @Override
    public ItemResponseDto getItem(Long id) {
        Item item = itemRepository.findItemById(id);
        return new ItemResponseDto(item);
    }

    @Override
    @Transactional
    public void updateItemSellCount(Long id, int quantity) {
        Item item = itemRepository.findItemById(id);
        if (item == null) {
            throw new NotFoundException("Item not found");
        }
        item.updateItemSellCount(quantity);
        itemRepository.save(item);
    }

    @Override
    @Transactional
    public ItemResponseDto updateQuantity(Long itemId, int quantity) {
        Item item = itemRepository.findItemById(itemId);
        if (item == null) {
            throw new NotFoundException("Item not found");
        }
        if (quantity == 0) {
            throw new IllegalArgumentException("Quantity cannot be zero");
        }
        item.updateStock(quantity);
        itemRepository.save(item);
        return new ItemResponseDto(item);
    }
}