package nodingo.core.global.auth.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReissueTokenResult {

    private final String accessToken;
    private final String refreshToken;

    public static ReissueTokenResult of(String accessToken, String refreshToken) {
        return new ReissueTokenResult(accessToken, refreshToken);
    }
}
