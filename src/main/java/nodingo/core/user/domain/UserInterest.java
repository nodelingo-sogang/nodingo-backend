package nodingo.core.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.global.domain.BaseTimeEntity;
import nodingo.core.keyword.domain.Keyword;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_interests",
        indexes = {
                @Index(name = "idx_user_target", columnList = "user_id, targetDate"),
                @Index(name = "idx_keyword", columnList = "keyword_id")
        }
)
public class UserInterest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private UserInterest parent;

    @Column(nullable = false)
    private LocalDate targetDate;

    public static UserInterest create(User user,
                                      Keyword keyword,
                                      InterestLevel level,
                                      UserInterest parent,
                                      LocalDate targetDate) {

        UserInterest interest = new UserInterest();
        interest.user = user;
        interest.keyword = keyword;
        interest.level = level;
        interest.parent = parent;
        interest.targetDate = targetDate;

        user.getInterestsInternal().add(interest);

        return interest;
    }
}
