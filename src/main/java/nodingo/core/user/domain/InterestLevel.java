package nodingo.core.user.domain;

import lombok.Getter;

@Getter
public enum InterestLevel {
    MACRO("중분류"),
    SPECIFIC("소분류");

    private final String description;

    InterestLevel(String description) {
        this.description = description;
    }
}
