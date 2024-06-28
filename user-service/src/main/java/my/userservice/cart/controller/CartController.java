package my.userservice.cart.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.userservice.cart.dto.CartItemResponseDto;
import my.userservice.cart.dto.CartResponseDto;
import my.userservice.cart.entity.Cart;
import my.userservice.cart.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 장바구니 조회
    @GetMapping
    public String getCart(HttpServletRequest request, Model model) {
        String username = request.getHeader("X-User-Name");
        List<CartItemResponseDto> cart = cartService.getCart(username);
        model.addAttribute("cart", cart);

        return "cart/cartlist";
    }

    // 장바구니 -> 주문 화면으로 이동
    @GetMapping("/order")
    public String cartOrder(HttpServletRequest request, Model model) {
        String username = request.getHeader("X-User-Name");
        List<CartItemResponseDto> cart = cartService.getCart(username);
        model.addAttribute("cart", cart);

        return "order/orderConfirm";
    }
}
