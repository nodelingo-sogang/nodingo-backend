package nodingo.core.keyword.domain;

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
        name = "recommend_keywords",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rec_keyword_user_date",
                        columnNames = {"user_id", "keyword_id", "targetDate"}
                )
        },
        indexes = {
                @Index(name = "idx_rec_kw_user_date", columnList = "user_id, targetDate"),
                @Index(name = "idx_rec_kw_user_date_score", columnList = "user_id, targetDate, score")
        }
)
public class RecommendKeyword extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    @Column(nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false)
    private double score;

    @Column(columnDefinition = "TEXT")
    private String summary;

    public static RecommendKeyword create(User user, Keyword keyword, LocalDate targetDate, double score) {
        RecommendKeyword recommendKeyword = new RecommendKeyword();
        recommendKeyword.user = user;
        recommendKeyword.keyword = keyword;
        recommendKeyword.targetDate = targetDate;
        recommendKeyword.score = score;
        return recommendKeyword;
    }

    // AI 브리핑 결과를 업데이트하는 메서드
    public void updateSummary(String summary) {
        this.summary= summary;
    }
}
