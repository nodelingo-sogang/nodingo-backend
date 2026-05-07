package nodingo.core.user.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import nodingo.core.user.domain.UserScrap;
import nodingo.core.user.repository.custom.UserScrapRepositoryCustom;

import java.util.Optional;

import static nodingo.core.user.domain.QUserScrap.userScrap;

@RequiredArgsConstructor
public class UserScrapRepositoryImpl implements UserScrapRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean isKeywordScrapped(Long userId, Long recommendKeywordId) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(userScrap)
                .where(
                        userScrap.user.id.eq(userId),
                        userScrap.recommendKeyword.id.eq(recommendKeywordId)
                )
                .fetchFirst();

        return fetchOne != null;
    }

    @Override
    public Optional<UserScrap> findKeywordScrap(Long userId, Long recommendKeywordId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(userScrap)
                        .where(
                                userScrap.user.id.eq(userId),
                                userScrap.recommendKeyword.id.eq(recommendKeywordId)
                        )
                        .fetchOne()
        );
    }
}
