package nodingo.core.keyword.repository.custom;
import nodingo.core.keyword.dto.query.KeywordCandidate;

import java.time.LocalDateTime;
import java.util.List;

public interface KeywordRepositoryCustom {
    List<KeywordCandidate> findCandidateKeywords(LocalDateTime startTime, LocalDateTime endTime);
}