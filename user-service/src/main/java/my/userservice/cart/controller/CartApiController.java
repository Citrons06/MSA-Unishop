package my.userservice.cart.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.userservice.cart.dto.AddItemCartRequest;
import my.userservice.cart.dto.UpdateCartItemRequest;
import my.userservice.cart.entity.Cart;
import my.userservice.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/internal/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    // 장바구니 조회
    @GetMapping
    public ResponseEntity<Cart> getCart(HttpServletRequest request) {
        // 헤더의 username 추출
        String username = request.getHeader("X-User-Name");
        Cart cart = cartService.getCart(username);
        return ResponseEntity.ok(cart);
    }

    // 장바구니에 상품 추가
    @PostMapping("/add")
    public ResponseEntity<Cart> addItemToCart(HttpServletRequest request, @RequestBody AddItemCartRequest addItemCartRequest) {
        String username = request.getHeader("X-User-Name");
        Cart cart = cartService.addCart(username, addItemCartRequest);
        return ResponseEntity.ok(cart);
    }

    // 장바구니에서 특정 상품 삭제
    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<Cart> removeItemFromCart(HttpServletRequest request, @PathVariable Long itemId) {
        String username = request.getHeader("X-User-Name");
        Cart cart = cartService.removeItem(username, itemId);
        return ResponseEntity.ok(cart);
    }

    // 장바구니 비우기
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        String username = request.getHeader("X-User-Name");
        cartService.clearCart(username);
        return ResponseEntity.ok("모든 상품이 장바구니에서 삭제되었습니다.");
    }

    // 장바구니에 담긴 상품의 수량 업데이트
    @PutMapping("/update/{itemId}")
    public ResponseEntity<Cart> updateItemQuantity(HttpServletRequest request,
                                                   @PathVariable Long itemId,
                                                   @RequestBody UpdateCartItemRequest updateCartItemRequest) {
        String username = request.getHeader("X-User-Name");
        Cart cart = cartService.updateCartItem(username, updateCartItemRequest);
        return ResponseEntity.ok(cart);
    }
}
