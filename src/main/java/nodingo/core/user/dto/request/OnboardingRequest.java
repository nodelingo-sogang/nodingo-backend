package nodingo.core.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
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

    @Size(min = 1, max = 2)
    private List<UserPersona> personas;

    @NotEmpty
    private List<InterestRequest> interests;

    public static OnboardingRequest of(List<UserPersona> personas, List<InterestRequest> interests) {
        return OnboardingRequest.builder()
                .personas(personas)
                .interests(interests)
                .build();
    }
}
