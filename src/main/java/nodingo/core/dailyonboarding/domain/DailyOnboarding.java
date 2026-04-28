package nodingo.core.dailyonboarding.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.global.domain.BaseTimeEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_onboarding")
public class DailyOnboarding extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate targetDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> questionsData;

    public static DailyOnboarding create(LocalDate targetDate, Map<String, Object> questionsData) {
        DailyOnboarding onboarding = new DailyOnboarding();
        onboarding.targetDate = targetDate;
        onboarding.questionsData = questionsData;
        return onboarding;
    }
}