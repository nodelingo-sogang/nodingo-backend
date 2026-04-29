package nodingo.core.user.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import nodingo.core.user.dto.request.InterestRequest;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class InterestCommand {

    private final Long macroId;
    private final List<Long> specificIds;

    public static InterestCommand from(InterestRequest request) {
        return InterestCommand.builder()
                .macroId(request.getMacroKeywordId())
                .specificIds(request.getSpecificKeywordIds())
                .build();
    }
}