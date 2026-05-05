package nodingo.core.keyword.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import nodingo.core.keyword.dto.query.KeywordCandidate;
import nodingo.core.keyword.dto.query.QKeywordCandidate;
import nodingo.core.keyword.repository.custom.KeywordRepositoryCustom;

import java.time.LocalDateTime;
import java.util.List;

import static nodingo.core.keyword.domain.QKeyword.keyword;
import static nodingo.core.keyword.domain.QNewsKeyword.newsKeyword;
import static nodingo.core.news.domain.QNews.news;

@RequiredArgsConstructor
public class KeywordRepositoryImpl implements KeywordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<KeywordCandidate> findCandidateKeywords(LocalDateTime startTime, LocalDateTime endTime) {
        return queryFactory
                .select(new QKeywordCandidate(
                        keyword.id,
                        keyword.word,
                        keyword.embedding
                ))
                .distinct()
                .from(keyword)
                .join(keyword.newsKeywords, newsKeyword)
                .join(newsKeyword.news, news)
                .where(
                        news.dateTimePub.between(startTime, endTime)
                )
                .fetch();
    }
}
