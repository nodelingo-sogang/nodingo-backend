package nodingo.core.keyword.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import nodingo.core.global.util.SliceUtil;
import nodingo.core.keyword.domain.KeywordRelation;
import nodingo.core.keyword.domain.QKeywordRelation;
import nodingo.core.keyword.repository.custom.KeywordRelationRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

@RequiredArgsConstructor
public class KeywordRelationRepositoryImpl implements KeywordRelationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<KeywordRelation> findTopRelations(Long keywordId, Pageable pageable) {
        QKeywordRelation kr = QKeywordRelation.keywordRelation;

        List<KeywordRelation> content = queryFactory
                .selectFrom(kr)
                .where(
                        kr.subjectKeyword.id.eq(keywordId) // 🚀 subjectKeyword로 수정
                                .or(kr.relatedKeyword.id.eq(keywordId)) // 🚀 relatedKeyword로 수정
                )
                .orderBy(kr.relationScore.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return SliceUtil.checkLastPage(pageable, content);
    }
}