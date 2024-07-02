package my.productservice.inventory.repository;


import my.productservice.inventory.entity.Inventory;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface InventoryRepository extends CrudRepository<Inventory, Long> {
    Optional<Inventory> findByItemId(Long itemId);
}
