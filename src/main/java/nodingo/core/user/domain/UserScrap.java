package nodingo.core.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.global.domain.BaseTimeEntity;
import nodingo.core.news.domain.News;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_scraps",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "news_id"})
        },
        indexes = {
                @Index(name = "idx_user_created", columnList = "user_id, createdAt")
        }
)
public class UserScrap extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    public static UserScrap create(User user, News news) {
        UserScrap scrap = new UserScrap();
        scrap.user = user;
        scrap.news = news;
        return scrap;
    }
}