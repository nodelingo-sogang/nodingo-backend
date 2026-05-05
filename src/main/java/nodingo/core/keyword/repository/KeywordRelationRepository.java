package nodingo.core.keyword.repository;

import nodingo.core.keyword.domain.KeywordRelation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRelationRepository extends JpaRepository<KeywordRelation, Long> {
}
