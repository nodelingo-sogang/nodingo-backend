package nodingo.core.global.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nodingo.core.global.auth.dto.result.ReissueTokenResult;

@Getter
@AllArgsConstructor
public class ReissueTokenResponse {

    private final String accessToken;
    private final String refreshToken;


    public static ReissueTokenResponse from(ReissueTokenResult result) {
        return new ReissueTokenResponse(result.getAccessToken(), result.getRefreshToken());
    }
}
