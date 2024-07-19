package my.userservice.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.userservice.cart.dto.AddItemCartRequest;
import my.userservice.cart.dto.CartItemResponseDto;
import my.userservice.cart.dto.CartResponseDto;
import my.userservice.cart.dto.UpdateCartItemRequest;
import my.userservice.cart.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class CartApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("장바구니 조회")
    void getCart_ShouldReturnCartItems_WhenUsernameIsValid() throws Exception {
        List<CartItemResponseDto> cartItems = Arrays.asList(
                new CartItemResponseDto(1L, "item1", 2, 100),
                new CartItemResponseDto(2L, "item2", 1, 200)
        );
        when(cartService.getCart(anyString())).thenReturn(cartItems);

        mockMvc.perform(get("/cart/api")
                        .header("X-User-Name", "username"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0].itemId").value(1L))
                .andExpect(jsonPath("$[0].itemName").value("item1"))
                .andExpect(jsonPath("$[1].itemId").value(2L))
                .andExpect(jsonPath("$[1].itemName").value("item2"));
    }

    @Test
    @DisplayName("장바구니에 상품 추가")
    void addItemToCart_ShouldAddItemAndReturnUpdatedCart() throws Exception {
        AddItemCartRequest request = new AddItemCartRequest(1L, "item1", 100, 2);
        CartItemResponseDto cartItemResponseDto = new CartItemResponseDto(1L, "item1", 2, 100);
        CartResponseDto responseDto = new CartResponseDto();
        when(cartService.addCart(anyString(), any(AddItemCartRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/cart/api/add")
                        .header("X-User-Name", "username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.itemsDto[0].itemId").value(1L))
                .andExpect(jsonPath("$.itemsDto[0].itemName").value("item1"));
    }

    @Test
    @DisplayName("장바구니에서 특정 상품 삭제")
    void removeItemFromCart_ShouldRemoveItemAndReturnUpdatedCart() throws Exception {
        CartResponseDto responseDto = new CartResponseDto();
        when(cartService.removeItem(anyString(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(delete("/cart/api/remove/{itemId}", 1L)
                        .header("X-User-Name", "username"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    @DisplayName("장바구니 비우기")
    void clearCart_ShouldClearCartAndReturnMessage() throws Exception {
        mockMvc.perform(delete("/cart/api/clear")
                        .header("X-User-Name", "username"))
                .andExpect(status().isOk())
                .andExpect(content().string("모든 상품이 장바구니에서 삭제되었습니다."));
    }

    @Test
    @DisplayName("장바구니에 담긴 상품의 수량 업데이트")
    void updateItemQuantity_ShouldUpdateQuantityAndReturnUpdatedCart() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(1L, null, null, 3);
        CartItemResponseDto cartItemResponseDto = new CartItemResponseDto(1L, "item1", 3, 150);
        CartResponseDto responseDto = new CartResponseDto();

        when(cartService.updateCartItem(anyString(), any(UpdateCartItemRequest.class))).thenReturn(responseDto);

        mockMvc.perform(put("/cart/api/update/{itemId}", 1L)
                        .header("X-User-Name", "username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.itemsDto[0].itemId").value(1L))
                .andExpect(jsonPath("$.itemsDto[0].count").value(3));
    }
}