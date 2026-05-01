package nodingo.core.user.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nodingo.core.keyword.domain.Keyword;

@Getter
@AllArgsConstructor
public class KeywordResult {
    private Long id;
    private String word;

    public static KeywordResult from(Keyword keyword) {
        return new KeywordResult(keyword.getId(), keyword.getWord());
    }
}