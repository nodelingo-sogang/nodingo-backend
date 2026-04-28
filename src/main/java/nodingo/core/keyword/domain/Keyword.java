package nodingo.core.keyword.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.global.domain.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "keywords",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"normalizedWord"})
        }
)
public class Keyword extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String word;

    @Column(nullable = false, length = 100)
    private String normalizedWord;

    @OneToMany(mappedBy = "keyword", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KeywordAlias> aliases = new ArrayList<>();

    public static Keyword create(String word) {
        Keyword keyword = new Keyword();
        keyword.word = word;
        keyword.normalizedWord = normalize(word);
        keyword.addAlias(word);
        return keyword;
    }

    public void addAlias(String alias) {
        KeywordAlias ka = KeywordAlias.create(this, alias);
        this.aliases.add(ka);
    }

    private static String normalize(String input) {
        return input.toLowerCase().trim();
    }
}