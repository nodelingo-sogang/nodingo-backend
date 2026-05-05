package nodingo.core.keyword.repository;

import nodingo.core.keyword.domain.RecommendKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface RecommendKeywordRepository extends JpaRepository<RecommendKeyword, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RecommendKeyword r WHERE r.user.id = :userId AND r.targetDate = :targetDate")
    void deleteByUserIdAndTargetDate(@Param("userId") Long userId, @Param("targetDate") LocalDate targetDate);
}