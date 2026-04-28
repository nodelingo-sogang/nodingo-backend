package nodingo.core.user.domain;

import jakarta.persistence.*;
import lombok.*;
import nodingo.core.global.domain.BaseTimeEntity;
import nodingo.core.keyword.domain.Keyword;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "providerId"})
        },
        indexes = {
                @Index(name = "idx_provider", columnList = "provider, providerId"),
                @Index(name = "idx_email", columnList = "email")
        }
)
public class User extends BaseTimeEntity implements UserDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerId;

    private String refreshToken;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_personas", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "persona")
    private List<UserPersona> personas = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserInterest> interests = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return this.username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    public static User create(String provider, String providerId, String username, String name, String email) {
        User user = new User();
        user.provider = provider;
        user.providerId = providerId;
        user.username = username;
        user.name = name;
        user.email = email;
        return user;
    }

    public void updateInfo(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void completeOnboarding(List<UserPersona> personas) {
        if (personas == null || personas.isEmpty()) {
            throw new IllegalArgumentException("최소 1개의 페르소나를 선택해야 합니다.");
        }
        if (personas.size() > 2) {
            throw new IllegalArgumentException("페르소나는 최대 2개까지만 선택 가능합니다.");
        }
        this.personas = personas;
    }

    public boolean isOnboardingCompleted() {
        return !this.personas.isEmpty();
    }

    public UserInterest addInterest(Keyword keyword,
                                    InterestLevel level,
                                    UserInterest parent,
                                    LocalDate targetDate) {

        UserInterest interest = UserInterest.create(this, keyword, level, parent, targetDate);
        this.interests.add(interest);
        return interest;
    }

    public List<UserInterest> getInterests() {
        return Collections.unmodifiableList(interests);
    }

    protected List<UserInterest> getInterestsInternal() {
        return interests;
    }
}