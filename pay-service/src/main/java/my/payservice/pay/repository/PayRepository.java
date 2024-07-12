package my.payservice.pay.repository;

import my.payservice.pay.entity.Pay;
import my.payservice.pay.entity.PayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PayRepository extends JpaRepository<Pay, Long> {
    Optional<Pay> findFirstByUsernameAndPayStatusOrderByCreatedDateDesc(String username, PayStatus payStatus);

    @Modifying
    @Query("UPDATE Pay p SET p.payStatus = :newStatus WHERE p.username = :username AND p.payStatus = :currentStatus")
    int updatePayStatus(@Param("username") String username,
                        @Param("currentStatus") PayStatus currentStatus,
                        @Param("newStatus") PayStatus newStatus);

    Optional<Pay> findFirstByUsernameOrderByCreatedDateDesc(String username);
}