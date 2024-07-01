package my.userservice.member.repository;

import my.userservice.member.entity.BlackList;
import org.springframework.data.repository.CrudRepository;

public interface BlacklistRepository extends CrudRepository<BlackList, Long> {
}
