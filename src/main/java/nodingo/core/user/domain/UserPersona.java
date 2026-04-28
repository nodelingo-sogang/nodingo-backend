package nodingo.core.user.domain;

import lombok.Getter;

@Getter
public enum UserPersona {
    POLITICS("정치"),
    ECONOMY("경제"),
    TECHNOLOGY("기술"),
    SOCIETY("사회"),
    CULTURE("문화"),
    INTERNATIONAL("국제");

    private final String description;
    UserPersona(String description) { this.description = description; }
}
