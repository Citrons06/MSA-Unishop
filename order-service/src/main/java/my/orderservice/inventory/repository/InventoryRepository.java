package my.orderservice.inventory.repository;


import my.orderservice.inventory.entity.Inventory;
import org.springframework.data.repository.CrudRepository;

public interface InventoryRepository extends CrudRepository<Inventory, Long> {
}
