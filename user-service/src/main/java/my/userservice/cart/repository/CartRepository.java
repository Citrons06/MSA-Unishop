package my.userservice.cart.repository;


import my.userservice.cart.entity.Cart;
import org.springframework.data.repository.CrudRepository;

public interface CartRepository extends CrudRepository<Cart, String> {
}