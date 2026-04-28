package nodingo.core.global.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.global.auth.CustomOAuth2User;
import nodingo.core.global.exception.user.UserNotFoundException;
import nodingo.core.user.domain.User;
import nodingo.core.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;
    private final UserRepository userRepository;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity,
            UserRepository userRepository) {

        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.accessTokenValidity = accessTokenValidity * 1000;
        this.refreshTokenValidity = refreshTokenValidity * 1000;
        this.userRepository = userRepository;
    }

    public String createAccessToken(Authentication authentication) {
        User user = extractUser(authentication);
        Date now = new Date();
        Date validity = new Date(now.getTime() + this.accessTokenValidity);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("auth", "ROLE_USER")
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Authentication authentication) {
        User user = extractUser(authentication);
        Date now = new Date();
        Date validity = new Date(now.getTime() + this.refreshTokenValidity);

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String username = claims.getSubject();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + username));
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(user, new HashMap<>());
        return new UsernamePasswordAuthenticationToken(customOAuth2User, token, user.getAuthorities());
    }

    public Authentication getAuthenticationFromUser(User user) {
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(user, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("유효하지 않은 JWT 토큰: {}", e.getMessage());
            return false;
        }
    }

    public long getRemainingTime(String token) {
        try {
            Claims claims = parseClaims(token);
            long expirationTime = claims.getExpiration().getTime();
            long now = System.currentTimeMillis();
            return Math.max(0, expirationTime - now);
        } catch (Exception e) {
            return 0;
        }
    }

    // ---------------------- Helper Methods ----------------------

    private User extractUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        if (principal instanceof CustomOAuth2User customUser) {
            return customUser.getUser();
        }
        throw new IllegalArgumentException("지원하지 않는 인증 주체 타입입니다: " + principal.getClass().getName());
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}