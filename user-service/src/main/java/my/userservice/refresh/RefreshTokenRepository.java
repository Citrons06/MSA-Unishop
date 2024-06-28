package my.userservice.refresh;

import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    void deleteByToken(String refreshToken);
    boolean existsByToken(String refreshToken);
}
