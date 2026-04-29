package nodingo.core.user.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import nodingo.core.user.domain.UserPersona;
import nodingo.core.user.dto.request.OnboardingRequest;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class SaveOnboardingCommand {

    private final Long userId;
    private final List<UserPersona> personas;
    private final InterestCommand interest;

    public static SaveOnboardingCommand from(Long userId, OnboardingRequest request) {
        return SaveOnboardingCommand.builder()
                .userId(userId)
                .personas(request.getPersonas())
                .interest(InterestCommand.from(request.getInterest()))
                .build();
    }
}