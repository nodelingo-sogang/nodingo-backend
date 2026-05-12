package nodingo.core.graph.service.query;

import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.graphPreview.GraphPreview;
import nodingo.core.graph.dto.result.GraphDataResult;
import nodingo.core.graph.dto.result.NodeSummaryResult;
import nodingo.core.graph.dto.result.TabListResult;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.keyword.domain.KeywordRelation;
import nodingo.core.keyword.domain.RecommendKeyword;
import nodingo.core.keyword.repository.KeywordRelationRepository;
import nodingo.core.keyword.repository.RecommendKeywordRepository;
import nodingo.core.user.domain.UserPersona;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphQueryServiceTest {

    @InjectMocks
    private GraphQueryService graphQueryService;

    @Mock
    private RecommendKeywordRepository recommendKeywordRepository;

    @Mock
    private KeywordRelationRepository keywordRelationRepository;

    @Mock
    private AiClient aiClient;

    @Test
    @DisplayName("API 1: 오늘의 추천 탭 목록 조회 테스트")
    void getTodayTabs_Success() {
        // given
        Long userId = 1L;
        RecommendKeyword rk1 = createRecommendKeyword(101L, "트럼프", 0.9);
        RecommendKeyword rk2 = createRecommendKeyword(102L, "관세", 0.8);

        when(recommendKeywordRepository.findAllWithKeyword(userId))
                .thenReturn(List.of(rk1, rk2));

        // when
        TabListResult result = graphQueryService.getTodayTabs(userId);

        // then
        assertThat(result.getTabs()).hasSize(2);
        assertThat(result.getTabs().get(0).getWord()).isEqualTo("트럼프");
        verify(recommendKeywordRepository, times(1)).findAllWithKeyword(userId);
    }

    @Test
    @DisplayName("API 2-1: 그래프 프리뷰 전체 조회 테스트 (keywordId가 null일 때)")
    void getGraphPreview_GlobalView_Success() {
        // given
        Long userId = 1L;
        RecommendKeyword rk1 = createRecommendKeyword(101L, "트럼프", 0.9);
        RecommendKeyword rk2 = createRecommendKeyword(102L, "관세", 0.8);

        // 1. 유저의 추천 키워드 목록 Mocking
        when(recommendKeywordRepository.findAllWithKeyword(userId)).thenReturn(List.of(rk1, rk2));

        // 2. 전체 보기용 findAllRelationsIn Mocking
        when(keywordRelationRepository.findAllRelationsIn(anyList())).thenReturn(List.of());

        GraphPreview.Response mockAiResponse = GraphPreview.Response.builder()
                .nodes(List.of(
                        GraphPreview.GraphNode.builder().id(101L).label("트럼프").build(),
                        GraphPreview.GraphNode.builder().id(102L).label("관세").build()
                ))
                .edges(List.of())
                .build();

        when(aiClient.getGraphPreview(any())).thenReturn(mockAiResponse);

        // when (keywordId를 null로 전달)
        GraphDataResult result = graphQueryService.getGraphPreview(userId, null);

        // then
        assertThat(result.getNodes()).hasSize(2);
        verify(keywordRelationRepository, times(1)).findAllRelationsIn(anyList());
        verify(keywordRelationRepository, never()).findTopRelations(anyLong(), any());
    }

    @Test
    @DisplayName("API 2-2: 그래프 프리뷰 특정 탭 조회 테스트 (keywordId가 존재할 때)")
    void getGraphPreview_FocusView_Success() {
        // given
        Long userId = 1L;
        Long keywordId = 101L;
        RecommendKeyword rk = createRecommendKeyword(keywordId, "트럼프", 0.9);

        when(recommendKeywordRepository.findAllWithKeyword(userId)).thenReturn(List.of(rk));

        Slice<KeywordRelation> slice = new SliceImpl<>(List.of());
        when(keywordRelationRepository.findTopRelations(eq(keywordId), any())).thenReturn(slice);

        GraphPreview.Response mockAiResponse = GraphPreview.Response.builder()
                .nodes(List.of(GraphPreview.GraphNode.builder().id(keywordId).label("트럼프").build()))
                .edges(List.of())
                .build();

        when(aiClient.getGraphPreview(any())).thenReturn(mockAiResponse);

        // when
        GraphDataResult result = graphQueryService.getGraphPreview(userId, keywordId);

        // then
        assertThat(result.getNodes()).isNotEmpty();
        assertThat(result.getNodes().get(0).getLabel()).isEqualTo("트럼프");
        verify(keywordRelationRepository, times(1)).findTopRelations(eq(keywordId), any());
        verify(keywordRelationRepository, never()).findAllRelationsIn(anyList());
    }

    @Test
    @DisplayName("API 3: 특정 노드 요약 조회 테스트")
    void getNodeSummary_Success() {
        // given
        Long userId = 1L;
        Long keywordId = 101L;
        RecommendKeyword rk = createRecommendKeyword(keywordId, "트럼프", 0.9);

        when(recommendKeywordRepository.findRecommend(userId, keywordId))
                .thenReturn(Optional.of(rk));

        // when
        NodeSummaryResult result = graphQueryService.getNodeSummary(userId, keywordId);

        // then
        assertThat(result.getSummary()).isEqualTo("테스트 요약본입니다.");
        assertThat(result.getWord()).isEqualTo("트럼프");
    }

    private RecommendKeyword createRecommendKeyword(Long id, String word, double score) {
        Keyword k = Keyword.create(word);
        ReflectionTestUtils.setField(k, "id", id);
        ReflectionTestUtils.setField(k, "persona", UserPersona.POLITICS);

        RecommendKeyword rk = RecommendKeyword.create(null, k, LocalDate.now(), score);

        ReflectionTestUtils.setField(rk, "id", 1L);
        rk.updateSummary("테스트 요약본입니다.");

        return rk;
    }
}