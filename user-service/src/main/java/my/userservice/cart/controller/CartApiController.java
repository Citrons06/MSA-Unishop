package my.userservice.cart.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.userservice.cart.dto.AddItemCartRequest;
import my.userservice.cart.dto.CartItemResponseDto;
import my.userservice.cart.dto.CartResponseDto;
import my.userservice.cart.dto.UpdateCartItemRequest;
import my.userservice.cart.service.CartService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cart/api")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    // 장바구니 조회
    @GetMapping
    public ResponseEntity<List<CartItemResponseDto>> getCart(HttpServletRequest request) {
        // 헤더의 username 추출
        String username = request.getHeader("X-User-Name");
        List<CartItemResponseDto> cart = cartService.getCart(username);
        log.info("username: {}, role: {}", request.getHeader("X-User-Name"), request.getHeader("X-User-Role"));
        return ResponseEntity.ok(cart);
    }

    // 장바구니에 상품 추가
    @PostMapping("/add")
    public ResponseEntity<CartResponseDto> addItemToCart(HttpServletRequest request, @RequestBody AddItemCartRequest addItemCartRequest) {
        String username = request.getHeader("X-User-Name");
        CartResponseDto cart = cartService.addCart(username, addItemCartRequest);
        return ResponseEntity.ok(cart);
    }

    // 장바구니에서 특정 상품 삭제
    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<CartResponseDto> removeItemFromCart(HttpServletRequest request, @PathVariable("itemId") Long itemId) {
        String username = request.getHeader("X-User-Name");
        CartResponseDto cart = cartService.removeItem(username, itemId);
        return ResponseEntity.ok(cart);
    }

    // 장바구니 비우기
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        String username = request.getHeader("X-User-Name");
        cartService.clearCart(username);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body("{\"msg\" : \"모든 상품이 장바구니에서 삭제되었습니다.\"}");
    }

    // 장바구니에 담긴 상품의 수량 업데이트
    @PutMapping("/update/{itemId}")
    public ResponseEntity<CartResponseDto> updateItemQuantity(HttpServletRequest request,
                                                   @PathVariable("itemId") Long itemId,
                                                   @RequestBody UpdateCartItemRequest updateCartItemRequest) {
        String username = request.getHeader("X-User-Name");
        CartResponseDto cart = cartService.updateCartItem(username, updateCartItemRequest);
        return ResponseEntity.ok(cart);
    }
}
