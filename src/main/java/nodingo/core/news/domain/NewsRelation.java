package nodingo.core.news.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.global.domain.BaseTimeEntity;
import nodingo.core.global.exception.news.NewsIllegalException;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "news_relations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_news_relation",
                        columnNames = {"subject_news_id", "related_news_id"}
                )
        },
        indexes = {
                @Index(name = "idx_subject_news", columnList = "subject_news_id"),
                @Index(name = "idx_related_news", columnList = "related_news_id")
        }
)
public class NewsRelation extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_news_id", nullable = false)
    private News subjectNews;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_news_id", nullable = false)
    private News relatedNews;

    @Column(nullable = false)
    private double relationScore;

    public static NewsRelation create(News a, News b, double score) {
        validateNews(a, b);
        News subject = (a.getId() < b.getId()) ? a : b;
        News related = (a.getId() < b.getId()) ? b : a;
        NewsRelation relation = new NewsRelation();
        relation.subjectNews = subject;
        relation.relatedNews = related;
        relation.relationScore = score;
        return relation;
    }

    public void updateRelation(double score) {
        this.relationScore = score;
    }

    private static void validateNews(News a, News b) {
        if (a == null || b == null) {
            throw new NewsIllegalException("뉴스는 null일 수 없습니다.");
        }

        if (a.getId() == null || b.getId() == null) {
            throw new NewsIllegalException("뉴스가 먼저 저장된 후 relation을 생성해야 합니다.");
        }

        if (a.getId().equals(b.getId())) {
            throw new NewsIllegalException("자기 자신을 연결할 수 없습니다.");
        }
    }
}
