package my.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.orderservice.exception.CommonException;
import my.orderservice.exception.ErrorCode;
import my.orderservice.order.dto.OrderRequestDto;
import my.orderservice.order.dto.OrderResponseDto;
import my.orderservice.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class OrderApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주문 전체 내역 조회")
    void orderList_ShouldReturnOrders_WhenUsernameIsValid() throws Exception {
        List<OrderResponseDto> orders = Arrays.asList(
                new OrderResponseDto(1L, "item1", 2, 100),
                new OrderResponseDto(2L, "item2", 1, 200)
        );
        Mockito.when(orderService.getOrderList(anyString(), anyInt(), anyInt())).thenReturn(orders);

        mockMvc.perform(get("/api/order/list")
                        .header("X-User-Name", "username")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$[0].orderId").value(1L))
                .andExpect(jsonPath("$[0].itemName").value("item1"))
                .andExpect(jsonPath("$[1].orderId").value(2L))
                .andExpect(jsonPath("$[1].itemName").value("item2"));
    }

    @Test
    @DisplayName("주문 단건 상세 조회")
    void orderDetail_ShouldReturnOrder_WhenOrderIdIsValid() throws Exception {
        OrderResponseDto order = new OrderResponseDto(1L, "item1", 2, 100);
        Mockito.when(orderService.getOrderById(anyLong())).thenReturn(order);

        mockMvc.perform(get("/api/order/{orderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.itemName").value("item1"));
    }

    @Test
    @DisplayName("주문 생성")
    void createOrder_ShouldCreateOrder_WhenRequestIsValid() throws Exception {
        OrderRequestDto orderRequest = new OrderRequestDto(1L, "item1", 2, 100);
        Mockito.when(orderService.order(anyString(), any(OrderRequestDto.class))).thenReturn(orderRequest);

        mockMvc.perform(post("/api/order")
                        .header("X-User-Name", "username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.itemName").value("item1"));
    }

    @Test
    @DisplayName("주문 취소")
    void cancelOrder_ShouldCancelOrder_WhenOrderIdIsValid() throws Exception {
        mockMvc.perform(post("/api/order/cancel/{orderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("주문이 취소되었습니다."));
    }

    @Test
    @DisplayName("주문 취소 실패")
    void cancelOrder_ShouldThrowException_WhenCancelFails() throws Exception {
        Mockito.doThrow(new CommonException(ErrorCode.CANCEL_FAILED)).when(orderService).cancelOrder(anyLong());

        mockMvc.perform(post("/api/order/cancel/{orderId}", 1L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("반품 신청")
    void returnOrder_ShouldReturnOrder_WhenOrderIdIsValid() throws Exception {
        mockMvc.perform(post("/api/order/return/{orderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("반품 신청을 완료하였습니다."));
    }

    @Test
    @DisplayName("반품 신청 실패")
    void returnOrder_ShouldThrowException_WhenReturnFails() throws Exception {
        Mockito.doThrow(new CommonException(ErrorCode.RETURN_FAILED)).when(orderService).returnOrder(anyLong());

        mockMvc.perform(post("/api/order/return/{orderId}", 1L))
                .andExpect(status().isInternalServerError());
    }
}
