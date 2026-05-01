package nodingo.core.keyword.repository;

import nodingo.core.keyword.domain.Keyword;
import nodingo.core.user.domain.InterestLevel;
import nodingo.core.user.domain.UserPersona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    // 중분류 조회
    List<Keyword> findAllByPersonaAndLevel(UserPersona persona, InterestLevel level);

    // 특정 중분류 하위의 소분류 조회
    List<Keyword> findAllByParentIdAndLevel(Long parentId, InterestLevel level);
}
