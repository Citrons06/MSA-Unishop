package my.userservice.cart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.userservice.adapter.ProductAdapter;
import my.userservice.adapter.ProductDto;
import my.userservice.cart.dto.AddItemCartRequest;
import my.userservice.cart.dto.UpdateCartItemRequest;
import my.userservice.cart.entity.Cart;
import my.userservice.cart.entity.CartItem;
import my.userservice.util.RedisUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisUtils redisUtils;
    private final ProductAdapter productAdapter;

    private static final long CART_TTL = 30; // 장바구니 TTL (30일)

    // 장바구니 조회
    public Cart getCart(String username) {
        Cart cart = redisUtils.get(username, Cart.class);

        if (cart == null) {
            cart = new Cart();
            cart.setMemberId(username);
            redisUtils.setExpire(username, CART_TTL, TimeUnit.DAYS);
        } else {
            // TTL 연장
            redisUtils.setExpire(username, CART_TTL, TimeUnit.DAYS);
        }
        return cart;
    }

    // 장바구니에 상품 추가
    public Cart addCart(String username, AddItemCartRequest addItemCartRequest) {
        // 상품 정보를 상품 서비스에서 조회
        ProductDto productDto = productAdapter.getProduct(addItemCartRequest.getItemId());

        if (!"SELL".equals(productDto.getItemSellStatus())) {
            throw new IllegalArgumentException("해당 상품은 판매 중이 아닙니다.");
        }

        if (addItemCartRequest.getQuantity() > productDto.getQuantity()) {
            throw new IllegalArgumentException("주문하려는 수량이 재고 수량보다 많습니다.");
        }

        Cart cart = redisUtils.get(username, Cart.class);

        if (cart == null) {
            cart = new Cart();
            cart.setMemberId(username);
        }

        // 이전에 같은 상품이 있는지 확인
        Optional<CartItem> itemOptional = cart.getItems().stream()
                .filter(item -> item.getId().equals(addItemCartRequest.getItemId()))
                .findFirst();

        if (itemOptional.isPresent()) {
            // 같은 상품이 이미 존재하는 경우 수량을 업데이트
            CartItem existingItem = itemOptional.get();
            existingItem.setQuantity(existingItem.getQuantity() + addItemCartRequest.getQuantity());
        } else {
            // 같은 상품이 없는 경우 새로운 상품을 장바구니에 추가
            CartItem newItem = new CartItem();
            newItem.setId(addItemCartRequest.getItemId());
            newItem.setItemName(productDto.getItemName());
            newItem.setPrice(productDto.getPrice());
            newItem.setQuantity(addItemCartRequest.getQuantity());
            cart.getItems().add(newItem);
        }

        redisUtils.put(username, cart);
        redisUtils.setExpire(username, CART_TTL, TimeUnit.DAYS); // TTL 설정
        return cart;
    }

    // 장바구니에서 특정 상품 제거
    public Cart removeItem(String username, Long itemId) {
        Cart cart = redisUtils.get(username, Cart.class);

        if (cart != null) {
            cart.getItems().removeIf(item -> item.getId().equals(itemId));
            redisUtils.put(username, cart);
            redisUtils.setExpire(username, CART_TTL, TimeUnit.DAYS); // TTL 설정
        }

        return cart;
    }

    // 장바구니 초기화
    public void clearCart(String username) {
        redisUtils.deleteData(username);
    }

    // 장바구니 상품 수량 업데이트
    public Cart updateCartItem(String username, UpdateCartItemRequest updateCartItemRequest) {
        Cart cart = redisUtils.get(username, Cart.class);

        if (cart != null) {
            Optional<CartItem> itemOptional = cart.getItems().stream()
                    .filter(item -> item.getId().equals(updateCartItemRequest.getItemId()))
                    .findFirst();

            if (itemOptional.isPresent()) {
                CartItem existingItem = itemOptional.get();
                existingItem.setQuantity(updateCartItemRequest.getQuantity());
                redisUtils.put(username, cart);
                redisUtils.setExpire(username, CART_TTL, TimeUnit.DAYS); // TTL 설정
            }
        }

        return cart;
    }
}