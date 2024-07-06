package my.payservice.pay.repository;

import my.payservice.pay.entity.Pay;
import my.payservice.pay.entity.PayStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayRepository extends JpaRepository<Pay, Long> {
    Optional<Pay> findFirstByUsernameAndPayStatusOrderByCreatedDateDesc(String username, PayStatus payStatus);

}
