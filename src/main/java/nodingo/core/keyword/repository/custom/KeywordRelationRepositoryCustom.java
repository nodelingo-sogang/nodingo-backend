package nodingo.core.keyword.repository.custom;

import nodingo.core.keyword.domain.KeywordRelation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface KeywordRelationRepositoryCustom {
    Slice<KeywordRelation> findTopRelations(Long keywordId, Pageable pageable);
    List<KeywordRelation> findAllRelationsIn(List<Long> keywordIds);
}
