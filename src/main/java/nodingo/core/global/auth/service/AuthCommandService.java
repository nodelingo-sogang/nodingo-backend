package nodingo.core.global.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.global.auth.dto.response.ReissueTokenResponse;
import nodingo.core.global.auth.jwt.JwtTokenProvider;
import nodingo.core.user.domain.User;
import nodingo.core.user.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthCommandService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 토큰 재발급
     */
    public ReissueTokenResponse reissue(String accessToken, String refreshToken) {
        validateForReissue(accessToken, refreshToken);
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 Refresh Token입니다."));

        Authentication auth = jwtTokenProvider.getAuthenticationFromUser(user);
        String newAccessToken = jwtTokenProvider.createAccessToken(auth);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(auth);
        user.updateRefreshToken(newRefreshToken);

        return new ReissueTokenResponse(newAccessToken, newRefreshToken);
    }

    /**
     * 로그아웃
     */
    public void logout(User user, String accessToken) {
        user.updateRefreshToken(null);
        userRepository.save(user);
        registerBlacklist(accessToken);
    }

    /******************** Helper Methods ********************/

    private void validateForReissue(String accessToken, String refreshToken) {
        if (accessToken != null && isBlacklisted(accessToken)) {
            throw new RuntimeException("이미 로그아웃된 토큰입니다.");
        }
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token이 만료되었거나 유효하지 않습니다.");
        }
    }

    private boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + accessToken));
    }

    private void registerBlacklist(String accessToken) {
        if (accessToken == null) return;

        long remainingMillis = jwtTokenProvider.getRemainingTime(accessToken);
        if (remainingMillis > 0) {
            redisTemplate.opsForValue().set(
                    "blacklist:" + accessToken,
                    "logout",
                    remainingMillis,
                    TimeUnit.MILLISECONDS
            );
            log.info("블랙리스트 등록 완료 (남은 시간: {}ms)", remainingMillis);
        }
    }
}
