package nodingo.core.user.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class KeywordListResult {
    private List<KeywordResult> contents;

    public static KeywordListResult from(List<KeywordResult> contents) {
        return new KeywordListResult(contents);
    }
}