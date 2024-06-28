package my.orderservice.order.repository;

import my.orderservice.order.entity.Order;
import my.orderservice.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByOrderStatusAndReturnRequestDateBefore(OrderStatus orderStatus, LocalDateTime localDateTime);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    @Query("SELECT o FROM orders o WHERE o.orderUsername = :username")
    Page<Order> findByOrderUsername(String username, Pageable pageable);
}
