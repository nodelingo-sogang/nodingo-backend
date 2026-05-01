package nodingo.core.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.user.dto.result.KeywordListResult;

import java.util.List;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KeywordListResponse {
    private List<KeywordResponse> contents;

    public static KeywordListResponse from(KeywordListResult result) {
        List<KeywordResponse> responses = result.getContents().stream()
                .map(KeywordResponse::from)
                .toList();
        return new KeywordListResponse(responses);
    }
}