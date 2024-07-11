package my.productservice.item.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import my.productservice.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemImgList LEFT JOIN i.category WHERE i.itemName LIKE %:name%")
    Page<Item> findByItemNameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemImgList LEFT JOIN i.category WHERE i.category.id = :categoryId AND i.itemName LIKE %:name%")
    Page<Item> findByCategoryIdAndItemNameContaining(@Param("categoryId") Long categoryId, @Param("name") String name, Pageable pageable);

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemImgList LEFT JOIN i.category")
    Page<Item> findAllWithImagesAndCategory(Pageable pageable);

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemImgList LEFT JOIN i.category WHERE i.category.id = :categoryId")
    Page<Item> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.itemImgList LEFT JOIN i.category WHERE i.id = :id")
    Item findItemById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.id = :id")
    Item findItemByIdWithLock(@Param("id") Long id);

}