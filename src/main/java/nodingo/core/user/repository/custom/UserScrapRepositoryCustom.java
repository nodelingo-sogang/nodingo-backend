package nodingo.core.user.repository.custom;

import nodingo.core.user.domain.UserScrap;

import java.util.Optional;

public interface UserScrapRepositoryCustom {
    boolean isKeywordScrapped(Long userId, Long recommendKeywordId);
    Optional<UserScrap> findKeywordScrap(Long userId, Long recommendKeywordId);
}
