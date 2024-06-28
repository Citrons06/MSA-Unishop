package my.productservice.admin.repository;


import my.productservice.item.entity.ItemImg;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemImgRepository extends JpaRepository<ItemImg, Long> {
}
