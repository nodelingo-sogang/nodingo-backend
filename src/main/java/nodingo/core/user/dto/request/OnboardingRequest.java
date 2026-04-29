package nodingo.core.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nodingo.core.user.domain.UserPersona;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingRequest {

    @Size(min = 1, max = 1)
    @NotNull
    private List<UserPersona> personas;

    @NotNull
    private InterestRequest interest;

    public static OnboardingRequest of(List<UserPersona> personas, InterestRequest interest) {
        return OnboardingRequest.builder()
                .personas(personas)
                .interest(interest)
                .build();
    }
}