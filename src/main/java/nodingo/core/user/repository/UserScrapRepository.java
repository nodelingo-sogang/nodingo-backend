package nodingo.core.user.repository;

import nodingo.core.user.domain.UserScrap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserScrapRepository extends JpaRepository<UserScrap, Long> {
    boolean existsByUserIdAndNewsId(Long userId, Long newsId);

    Optional<UserScrap> findByUserIdAndNewsId(Long userId, Long newsId);
}
