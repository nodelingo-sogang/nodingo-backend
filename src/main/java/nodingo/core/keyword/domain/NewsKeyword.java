package nodingo.core.keyword.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.news.domain.News;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "news_keywords",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"news_id", "keyword_id"})
        },
        indexes = {
                @Index(name = "idx_news_weight", columnList = "news_id, weight"),
                @Index(name = "idx_keyword_weight", columnList = "keyword_id, weight")
        }
)
public class NewsKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    @Column(nullable = false)
    private double weight;

    public static NewsKeyword create(News news, Keyword keyword, double weight) {
        NewsKeyword newsKeyword = new NewsKeyword();
        newsKeyword.news = news;
        newsKeyword.keyword = keyword;
        newsKeyword.weight = weight;
        return newsKeyword;
    }
}