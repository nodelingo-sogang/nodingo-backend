package nodingo.core.graph.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.graphPreview.GraphPreview;
import nodingo.core.global.exception.recommendKeyword.RecommendKeywordNotFoundException;
import nodingo.core.graph.dto.result.GraphDataResult;
import nodingo.core.graph.dto.result.NodeSummaryResult;
import nodingo.core.graph.dto.result.TabListResult;
import nodingo.core.graph.dto.result.TabResult;
import nodingo.core.keyword.domain.KeywordRelation;
import nodingo.core.keyword.domain.RecommendKeyword;
import nodingo.core.keyword.repository.KeywordRelationRepository;
import nodingo.core.keyword.repository.RecommendKeywordRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GraphQueryService {
    private final static int KEYWORD_LIMIT=4;
    private final static int RELATED_KEYWORD_LIMIT=8;
    private final AiClient aiClient;
    private final RecommendKeywordRepository recommendKeywordRepository;
    private final KeywordRelationRepository keywordRelationRepository;

    public TabListResult getTodayTabs(Long userId) {
        List<RecommendKeyword> recommendKeywords = recommendKeywordRepository.findAllWithKeyword(userId);

        List<TabResult> tabs = recommendKeywords.stream()
                .sorted(Comparator.comparingDouble(RecommendKeyword::getScore).reversed())
                .limit(KEYWORD_LIMIT)
                .map(TabResult::of)
                .toList();

        return TabListResult.of(tabs);
    }

    @Cacheable(value = "batch:graph", key = "#userId + ':' + #centralKeywordId")
    public GraphDataResult getGraphPreview(Long userId, Long centralKeywordId) {
        log.info("[GraphQueryService] Generating customized graph - User: {}, CentralKeyword: {}", userId, centralKeywordId);

        Map<Long, RecommendKeyword> recommendMap = getRecommendKeywordMap(userId);

        // 2. 중심 키워드 기준 상위 8개 관계 조회
        List<KeywordRelation> topRelations = keywordRelationRepository
                .findTopRelations(centralKeywordId, PageRequest.of(0, RELATED_KEYWORD_LIMIT))
                .getContent();

        // 3. AI 서버(FastAPI)용 Request 빌드
        GraphPreview.Request aiRequest = createAiRequest(centralKeywordId, topRelations, recommendMap);

        // 4. FastAPI 서버 호출 및 결과 변환
        GraphPreview.Response aiResponse = aiClient.getGraphPreview(aiRequest);

        return GraphDataResult.from(aiResponse);
    }

    public NodeSummaryResult getNodeSummary(Long userId, Long keywordId) {
        RecommendKeyword recommendKeyword = getOrElseThrow(userId, keywordId);
        return NodeSummaryResult.from(recommendKeyword);
    }

    private Map<Long, RecommendKeyword> getRecommendKeywordMap(Long userId) {
        return recommendKeywordRepository.findAllWithKeyword(userId).stream()
                .collect(Collectors.toMap(rk -> rk.getKeyword().getId(), rk -> rk));
    }

    private GraphPreview.Request createAiRequest(Long centralId, List<KeywordRelation> relations, Map<Long, RecommendKeyword> recommendMap) {
        Set<Long> nodeIds = extractAllNodeIds(centralId, relations);

        return GraphPreview.Request.builder()
                .recommendKeywords(mapToKeywordInputs(nodeIds, recommendMap))
                .keywordRelations(mapToRelationInputs(relations))
                .build();
    }

    private Set<Long> extractAllNodeIds(Long centralId, List<KeywordRelation> relations) {
        Set<Long> ids = new HashSet<>();
        ids.add(centralId);
        relations.forEach(rel -> {
            ids.add(rel.getSubjectKeyword().getId());
            ids.add(rel.getRelatedKeyword().getId());
        });
        return ids;
    }

    private List<GraphPreview.GraphRecommendKeywordInput> mapToKeywordInputs(Set<Long> nodeIds, Map<Long, RecommendKeyword> recommendMap) {
        return nodeIds.stream()
                .map(id -> {
                    RecommendKeyword rk = recommendMap.get(id);
                    return GraphPreview.GraphRecommendKeywordInput.builder()
                            .keywordId(id)
                            .word(rk != null ? rk.getKeyword().getWord() : "Unknown")
                            .score(rk != null ? rk.getScore() : 1.0)
                            .summary(rk != null ? rk.getSummary() : "")
                            .persona(rk != null && rk.getKeyword().getPersona() != null ?
                                    rk.getKeyword().getPersona().name() : null)
                            .build();
                }).toList();
    }

    private List<GraphPreview.GraphKeywordRelationInput> mapToRelationInputs(List<KeywordRelation> relations) {
        return relations.stream()
                .map(rel -> GraphPreview.GraphKeywordRelationInput.builder()
                        .sourceKeywordId(rel.getSubjectKeyword().getId())
                        .targetKeywordId(rel.getRelatedKeyword().getId())
                        .relationScore(rel.getRelationScore())
                        .build())
                .toList();
    }

    private RecommendKeyword getOrElseThrow(Long userId, Long keywordId) {
        return recommendKeywordRepository.findRecommend(userId, keywordId)
                .orElseThrow(() -> new RecommendKeywordNotFoundException("추천 키워드를 찾을 수 없습니다."));
    }

}