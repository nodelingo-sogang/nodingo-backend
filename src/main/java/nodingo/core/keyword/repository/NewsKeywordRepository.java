package nodingo.core.keyword.repository;

import nodingo.core.keyword.domain.NewsKeyword;
import nodingo.core.keyword.repository.custom.NewsKeywordRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsKeywordRepository extends JpaRepository<NewsKeyword, Long>, NewsKeywordRepositoryCustom {
}