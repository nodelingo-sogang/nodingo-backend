package nodingo.core.keyword.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.global.domain.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "keyword_relations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_keyword_relation",
                        columnNames = {"subject_keyword_id", "related_keyword_id"}
                )
        },
        indexes = {
                @Index(name = "idx_subject_keyword", columnList = "subject_keyword_id"),
                @Index(name = "idx_related_keyword", columnList = "related_keyword_id")
        }
)
public class KeywordRelation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_keyword_id", nullable = false)
    private Keyword subjectKeyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_keyword_id", nullable = false)
    private Keyword relatedKeyword;

    @Column(nullable = false)
    private double relationScore;

    public static KeywordRelation create(Keyword a, Keyword b, double score) {
        validateKeywords(a, b);

        Keyword subject = (a.getId() < b.getId()) ? a : b;
        Keyword related = (a.getId() < b.getId()) ? b : a;

        KeywordRelation relation = new KeywordRelation();
        relation.subjectKeyword = subject;
        relation.relatedKeyword = related;
        relation.relationScore = score;
        return relation;
    }

    public void updateRelation(double score) {
        this.relationScore = score;
    }

    private static void validateKeywords(Keyword a, Keyword b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("키워드는 null일 수 없습니다.");
        }

        if (a.getId() == null || b.getId() == null) {
            throw new IllegalStateException("키워드가 영속화된 후 관계를 생성할 수 있습니다.");
        }

        if (a.getId().equals(b.getId())) {
            throw new IllegalArgumentException("자기 자신과 관계를 맺을 수 없습니다.");
        }
    }
}
