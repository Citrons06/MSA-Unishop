package my.productservice.item;

import my.productservice.item.dto.ItemResponseDto;
import my.productservice.item.service.ItemReadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class ItemApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemReadService itemReadService;

    @Test
    @DisplayName("아이템 목록 조회")
    void getItems_ShouldReturnItems_WhenRequestIsValid() throws Exception {
        List<ItemResponseDto> itemList = Arrays.asList(
                new ItemResponseDto(1L, "item1", 3, 100),
                new ItemResponseDto(2L, "item2", 5, 200)
        );
        Page<ItemResponseDto> items = new PageImpl<>(itemList);
        Mockito.when(itemReadService.getItems(any(PageRequest.class))).thenReturn(items);

        mockMvc.perform(get("/api/product/list")
                        .param("page", "0")
                        .param("size", "8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].itemId").value(1L))
                .andExpect(jsonPath("$.content[0].itemName").value("item1"))
                .andExpect(jsonPath("$.content[1].itemId").value(2L))
                .andExpect(jsonPath("$.content[1].itemName").value("item2"));
    }

    @Test
    @DisplayName("아이템 상세 조회")
    void getItem_ShouldReturnItem_WhenItemIdIsValid() throws Exception {
        ItemResponseDto item = new ItemResponseDto(1L, "item1", 3, 100);
        Mockito.when(itemReadService.getItem(anyLong())).thenReturn(item);

        mockMvc.perform(get("/api/product/{itemId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.itemId").value(1L))
                .andExpect(jsonPath("$.itemName").value("item1"));
    }

    @Test
    @DisplayName("아이템 검색")
    void searchItems_ShouldReturnItems_WhenSearchIsValid() throws Exception {
        List<ItemResponseDto> itemList = Arrays.asList(
                new ItemResponseDto(1L, "item1", 3, 100),
                new ItemResponseDto(2L, "item2", 5, 200)
        );
        Page<ItemResponseDto> items = new PageImpl<>(itemList);
        Mockito.when(itemReadService.searchItemsByName(anyString(), any(PageRequest.class))).thenReturn(items);

        mockMvc.perform(get("/api/product/search")
                        .param("search", "item")
                        .param("page", "0")
                        .param("size", "8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].itemId").value(1L))
                .andExpect(jsonPath("$.content[0].itemName").value("item1"))
                .andExpect(jsonPath("$.content[1].itemId").value(2L))
                .andExpect(jsonPath("$.content[1].itemName").value("item2"));
    }

    @Test
    @DisplayName("카테고리와 아이템 이름으로 검색")
    void searchItemsByCategory_ShouldReturnItems_WhenCategoryAndSearchAreValid() throws Exception {
        List<ItemResponseDto> itemList = Arrays.asList(
                new ItemResponseDto(1L, "item1", 3, 100),
                new ItemResponseDto(2L, "item2", 5, 200)
        );
        Page<ItemResponseDto> items = new PageImpl<>(itemList);
        Mockito.when(itemReadService.searchItemsByCategoryAndItemName(anyLong(), anyString(), any(PageRequest.class))).thenReturn(items);

        mockMvc.perform(get("/api/product/search")
                        .param("search", "item")
                        .param("category", "1")
                        .param("page", "0")
                        .param("size", "8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0].itemId").value(1L))
                .andExpect(jsonPath("$.content[0].itemName").value("item1"))
                .andExpect(jsonPath("$.content[1].itemId").value(2L))
                .andExpect(jsonPath("$.content[1].itemName").value("item2"));
    }
}
