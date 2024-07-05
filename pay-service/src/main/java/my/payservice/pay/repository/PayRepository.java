package my.payservice.pay.repository;

import my.payservice.pay.entity.Pay;
import my.payservice.pay.entity.PayStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayRepository extends JpaRepository<Pay, Long> {
    Pay findByUsernameAndPayStatus(String username, PayStatus payStatus);
}
