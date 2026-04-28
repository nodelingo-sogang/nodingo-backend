package nodingo.core.news.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.global.domain.BaseTimeEntity;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.domain.NewsKeyword;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "news",
        indexes = {
                @Index(name = "idx_published_at", columnList = "publishedAt")
        }
)
public class News extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String newsUri;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(nullable = false, unique = true)
    private String url;

    private String language;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "vector")
    private double[] embedding;

    @Column(nullable = false)
    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewsKeyword> newsKeywords = new ArrayList<>();

    public static News create(String newsUri,
                              String title,
                              String body,
                              String url,
                              String language,
                              LocalDateTime publishedAt) {
        News news = new News();
        news.newsUri = newsUri;
        news.title = title;
        news.body = body;
        news.url = url;
        news.language = language;
        news.publishedAt = publishedAt;
        return news;
    }

    public void addKeyword(Keyword keyword, double weight) {
        NewsKeyword nk = NewsKeyword.create(this, keyword, weight);
        this.newsKeywords.add(nk);
    }

    public void updateEmbedding(double[] embedding) {
        this.embedding = embedding;
    }
}