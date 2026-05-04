package nodingo.core.news.repository;

import nodingo.core.news.domain.NewsRelation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRelationRepository extends JpaRepository<NewsRelation, Long> {
}
