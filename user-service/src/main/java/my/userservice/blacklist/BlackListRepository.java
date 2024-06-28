package my.userservice.blacklist;


import org.springframework.data.repository.CrudRepository;

public interface BlackListRepository extends CrudRepository<BlackList, Long> {
    boolean existsByRefreshToken(String token);
}