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
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_news_uri", columnNames = {"uri"})
        },
        indexes = {
                @Index(name = "idx_news_date_time_pub", columnList = "dateTimePub")
        }
)
public class News extends BaseTimeEntity {

    private static final int EMBEDDING_DIMENSION = 1536;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Event Registry 고유 식별자 (7546825937)
    @Column(nullable = false, unique = true)
    private String uri;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String title;

    // 초기에는 원문 전체, 이후 LLM을 통해 200자 요약본으로 업데이트될 필드
    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(nullable = false, unique = true, length = 1000)
    private String url;

    // ISO3 언어 코드 (eng, kor 등)
    @Column(length = 10)
    private String lang;

    // API에서 제공하는 감성 분석 지수
    private Double sentiment;

    // 기사 발행 일시
    @Column(nullable = false)
    private LocalDateTime dateTimePub;

    // pgvector 임베딩
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;

    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewsKeyword> newsKeywords = new ArrayList<>();

    public static News create(String uri, String title, String body, String url,
                              String lang, Double sentiment, LocalDateTime dateTimePub) {
        News news = new News();
        news.uri = uri;
        news.title = title;
        news.body = body;
        news.url = url;
        news.lang = lang;
        news.sentiment = sentiment;
        news.dateTimePub = dateTimePub;
        news.embedding = emptyEmbedding();
        return news;
    }

    // 나중에 LLM 요약본으로 교체할 때 사용
    public void updateBody(String summarizedBody) {
        this.body = summarizedBody;
    }

    public void updateEmbedding(float[] embedding) {
        this.embedding = embedding != null ? embedding : emptyEmbedding();
    }

    public void addKeyword(Keyword keyword, double weight) {
        NewsKeyword nk = NewsKeyword.create(this, keyword, weight);
        this.newsKeywords.add(nk);
    }

    private static float[] emptyEmbedding() {
        return new float[EMBEDDING_DIMENSION];
    }
}