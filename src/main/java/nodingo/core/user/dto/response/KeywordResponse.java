package nodingo.core.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.user.dto.result.KeywordResult;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KeywordResponse {
    private Long id;
    private String word;

    public static KeywordResponse from(KeywordResult result) {
        return new KeywordResponse(result.getId(), result.getWord());
    }
}
