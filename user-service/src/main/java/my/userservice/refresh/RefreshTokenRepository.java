package my.userservice.refresh;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    void deleteByToken(String refreshToken);
    boolean existsByToken(String refreshToken);

    void deleteAllByUsername(String username);

    List<RefreshToken> findAllByUsername(String username);
}
