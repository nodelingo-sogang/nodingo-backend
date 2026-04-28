package nodingo.core.news.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import nodingo.core.global.util.SliceUtil;
import nodingo.core.news.domain.News;
import nodingo.core.news.dto.query.NewsResult;
import nodingo.core.news.dto.query.QNewsResult;
import nodingo.core.news.repository.custom.NewsRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

import static nodingo.core.news.domain.QKeyword.keyword;
import static nodingo.core.news.domain.QKeywordAlias.keywordAlias;
import static nodingo.core.news.domain.QNews.news;
import static nodingo.core.news.domain.QNewsKeyword.newsKeyword;


@RequiredArgsConstructor
public class NewsRepositoryImpl implements NewsRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public Slice<NewsResult> findRecentNewsByKeywords(List<String> keywordNames,
                                                      LocalDateTime limitTime,
                                                      Pageable pageable) {
        List<String> normalized = keywordNames.stream()
                .map(s -> s.toLowerCase().trim())
                .toList();

        List<NewsResult> content = queryFactory
                .selectDistinct(new QNewsResult(
                        news.id,
                        news.newsUri,
                        news.title,
                        news.body,
                        news.url,
                        news.imageUrl,
                        news.language,
                        news.publishedAt
                ))
                .from(news)
                .join(news.newsKeywords, newsKeyword)
                .join(newsKeyword.keyword, keyword)
                .leftJoin(keyword.aliases, keywordAlias)
                .where(
                        keyword.normalizedWord.in(normalized)
                                .or(keywordAlias.alias.in(normalized)),
                        news.publishedAt.goe(limitTime)
                )
                .orderBy(news.publishedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
        return SliceUtil.checkLastPage(pageable, content);
    }


    @Override
    public Slice<NewsResult> findNewsByLanguage(String language,
                                                LocalDateTime limitTime,
                                                Pageable pageable) {
        List<NewsResult> content = queryFactory
                .select(new QNewsResult(
                        news.id,
                        news.newsUri,
                        news.title,
                        news.body,
                        news.url,
                        news.imageUrl,
                        news.language,
                        news.publishedAt
                ))
                .from(news)
                .where(
                        news.language.eq(language),
                        news.publishedAt.goe(limitTime)
                )
                .orderBy(news.publishedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return SliceUtil.checkLastPage(pageable, content);
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<News> findTop5SimilarNews(Long newsId, int limit) {
        String sql = """
                SELECT n.*
                FROM news n
                WHERE n.id != :newsId
                  AND n.embedding IS NOT NULL
                ORDER BY n.embedding <=> (
                    SELECT embedding FROM news WHERE id = :newsId
                )
                LIMIT :limit
                """;

        return (List<News>) em.createNativeQuery(sql, News.class)
                .setParameter("newsId", newsId)
                .setParameter("limit", limit)
                .getResultList();
    }
}