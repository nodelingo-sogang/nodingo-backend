package nodingo.core.user.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestRequest {

    @NotNull
    private Long macroKeywordId;

    @Size(max = 6)
    private List<Long> specificKeywordIds;

    public static InterestRequest of(Long macroKeywordId, List<Long> specificKeywordIds) {
        return InterestRequest.builder()
                .macroKeywordId(macroKeywordId)
                .specificKeywordIds(specificKeywordIds)
                .build();
    }
}
