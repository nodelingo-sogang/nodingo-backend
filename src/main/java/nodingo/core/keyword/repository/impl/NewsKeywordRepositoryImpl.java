package nodingo.core.keyword.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import nodingo.core.keyword.domain.NewsKeyword;
import nodingo.core.keyword.repository.custom.NewsKeywordRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.util.List;

import static nodingo.core.keyword.domain.QNewsKeyword.newsKeyword;
import static nodingo.core.news.domain.QNews.news;

@Repository
@RequiredArgsConstructor
public class NewsKeywordRepositoryImpl implements NewsKeywordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<NewsKeyword> findTopByKeywordId(Long keywordId, int limit) {
        return queryFactory
                .selectFrom(newsKeyword)
                .join(newsKeyword.news, news).fetchJoin()
                .where(newsKeyword.keyword.id.eq(keywordId))
                .orderBy(newsKeyword.weight.desc())
                .limit(limit)
                .fetch();
    }
}