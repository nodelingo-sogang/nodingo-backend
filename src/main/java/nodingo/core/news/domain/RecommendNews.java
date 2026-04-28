package nodingo.core.news.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.global.domain.BaseTimeEntity;
import nodingo.core.user.domain.User;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "recommend_news",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "news_id", "targetDate"})
        },
        indexes = {
                @Index(name = "idx_rec_user_date", columnList = "user_id, targetDate"),
                @Index(name = "idx_rec_user_date_rank", columnList = "user_id, targetDate, ranking")
        }
)
public class RecommendNews extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @Column(nullable = false)
    private LocalDate targetDate;

    @Column(columnDefinition = "TEXT")
    private String recommendationReason;

    @Column(nullable = false)
    private int ranking;

    public static RecommendNews create(User user,
                                       News news,
                                       LocalDate targetDate,
                                       String recommendationReason,
                                       int ranking) {
        RecommendNews recommendNews = new RecommendNews();
        recommendNews.user = user;
        recommendNews.news = news;
        recommendNews.targetDate = targetDate;
        recommendNews.recommendationReason = recommendationReason;
        recommendNews.ranking = ranking;
        return recommendNews;
    }
}