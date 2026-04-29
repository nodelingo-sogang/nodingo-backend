package nodingo.core.news.repository;

import nodingo.core.news.domain.News;
import nodingo.core.news.repository.custom.NewsRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long>, NewsRepositoryCustom {

    boolean existsByUri(String uri);

    @Query("select n.uri from News n where n.uri in :uris")
    List<String> findExistingUris(@Param("uris") List<String> uris);
}
