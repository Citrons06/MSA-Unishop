/*
package my.productservice.item;

import my.productservice.admin.service.ItemAdminService;
import my.productservice.item.dto.item.CreateItemResponse;
import my.productservice.item.dto.item.ItemRequestDto;
import my.productservice.item.dto.item.ItemResponseDto;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class ItemApiAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemAdminService itemAdminService;

    @Test
    @DisplayName("아이템 생성")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createItem_ShouldCreateItem_WhenRequestIsValid() throws Exception {
        ItemRequestDto itemRequest = new ItemRequestDto();
        itemRequest.setItemName("item1");
        itemRequest.setPrice(100);
        itemRequest.setQuantity(10);

        MockMultipartFile file = new MockMultipartFile("itemImgFileList", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        CreateItemResponse cartItemResponse = new CreateItemResponse(1L, "item1", 100, 10);

        Mockito.when(itemAdminService.createItem(any(ItemRequestDto.class))).thenReturn(cartItemResponse);

        mockMvc.perform(multipart("/api/product/admin/create")
                        .file(file)
                        .param("categoryId", "1")
                        .param("itemName", "item1")
                        .param("price", "100")
                        .param("quantity", "10")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.itemId").value(1L))
                .andExpect(jsonPath("$.itemName").value("item1"));
    }

    @Test
    @DisplayName("아이템 상세 조회")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getItem_ShouldReturnItem_WhenItemIdIsValid() throws Exception {
        ItemResponseDto itemResponse = new ItemResponseDto(1L, "item1", 100, 10);

        Mockito.when(itemAdminService.getItem(anyLong())).thenReturn(itemResponse);

        mockMvc.perform(get("/api/product/admin/{itemId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.itemId").value(1L))
                .andExpect(jsonPath("$.itemName").value("item1"));
    }

    @Test
    @DisplayName("아이템 업데이트")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateItem_ShouldUpdateItem_WhenRequestIsValid() throws Exception {
        ItemRequestDto itemRequest = new ItemRequestDto();
        itemRequest.setItemName("updatedItem");
        itemRequest.setPrice(150);
        itemRequest.setQuantity(20);

        MockMultipartFile file = new MockMultipartFile("itemImgFileList", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        ItemResponseDto itemResponse = new ItemResponseDto(1L, "updatedItem", 150, 20);

        Mockito.when(itemAdminService.updateItem(anyLong(), any(ItemRequestDto.class), anyList(), anyLong())).thenReturn(itemResponse);

        mockMvc.perform(multipart("/api/product/admin/update/{itemId}", 1L)
                        .file(file)
                        .param("categoryId", "1")
                        .param("itemName", "updatedItem")
                        .param("price", "150")
                        .param("quantity", "20")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.itemId").value(1L))
                .andExpect(jsonPath("$.itemName").value("updatedItem"));
    }

    @Test
    @DisplayName("아이템 삭제")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteItem_ShouldDeleteItem_WhenItemIdIsValid() throws Exception {
        mockMvc.perform(delete("/api/product/admin/delete/{itemId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value("delete success"));
    }

    @Test
    @DisplayName("아이템 목록 조회")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getItems_ShouldReturnItems_WhenRequestIsValid() throws Exception {
        List<ItemResponseDto> itemList = Arrays.asList(
                new ItemResponseDto(1L, "item1", 100, 10),
                new ItemResponseDto(2L, "item2", 200, 20)
        );
        Page<ItemResponseDto> items = new PageImpl<>(itemList);
        Mockito.when(itemAdminService.getItems(any(PageRequest.class))).thenReturn(items);

        mockMvc.perform(get("/api/product/admin/list")
                        .param("page", "0")
                        .param("size", "8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.content[0].itemId").value(1L))
                .andExpect(jsonPath("$.content[0].itemName").value("item1"))
                .andExpect(jsonPath("$.content[1].itemId").value(2L))
                .andExpect(jsonPath("$.content[1].itemName").value("item2"));
    }

    @Test
    @DisplayName("아이템 검색")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void searchItems_ShouldReturnItems_WhenSearchIsValid() throws Exception {
        List<ItemResponseDto> itemList = Arrays.asList(
                new ItemResponseDto(1L, "item1", 100, 10),
                new ItemResponseDto(2L, "item2", 200, 20)
        );
        Page<ItemResponseDto> items = new PageImpl<>(itemList);
        Mockito.when(itemAdminService.searchItemsByName(anyString(), any(PageRequest.class))).thenReturn(items);

        mockMvc.perform(get("/api/product/admin/search")
                        .param("search", "item")
                        .param("page", "0")
                        .param("size", "8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.content[0].itemId").value(1L))
                .andExpect(jsonPath("$.content[0].itemName").value("item1"))
                .andExpect(jsonPath("$.content[1].itemId").value(2L))
                .andExpect(jsonPath("$.content[1].itemName").value("item2"));
    }

    @Test
    @DisplayName("카테고리와 아이템 이름으로 검색")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void searchItemsByCategory_ShouldReturnItems_WhenCategoryAndSearchAreValid() throws Exception {
        List<ItemResponseDto> itemList = Arrays.asList(
                new ItemResponseDto(1L, "item1", 100, 10),
                new ItemResponseDto(2L, "item2", 200, 20)
        );
        Page<ItemResponseDto> items = new PageImpl<>(itemList);
        Mockito.when(itemAdminService.searchItemsByCategoryAndItemName(anyLong(), anyString(), any(PageRequest.class))).thenReturn(items);

        mockMvc.perform(get("/api/product/admin/search")
                        .param("search", "item")
                        .param("category", "1")
                        .param("page", "0")
                        .param("size", "8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.content[0].itemId").value(1L))
                .andExpect(jsonPath("$.content[0].itemName").value("item1"))
                .andExpect(jsonPath("$.content[1].itemId").value(2L))
                .andExpect(jsonPath("$.content[1].itemName").value("item2"));
    }
}
*/
