package nodingo.core.keyword.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.keyword.KeywordRecommend;
import nodingo.core.keyword.domain.RecommendKeyword;
import nodingo.core.user.repository.UserInterestRepository;
import nodingo.core.keyword.repository.KeywordRepository;
import nodingo.core.keyword.repository.RecommendKeywordRepository;
import nodingo.core.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional

public class KeywordRecommendService {
    public static final int TOP_K_RECOMMENDATIONS = 12;

    private final AiClient aiClient;
    private final KeywordRepository keywordRepository;
    private final RecommendKeywordRepository recommendKeywordRepository;
    private final UserInterestRepository userInterestRepository;

    /**
     * [개별] 유저 한 명에 대해 파이썬 AI 호출 및 결과 DB 저장
     */
    public List<RecommendKeyword> generateRecommendationForUser(
            User user,
            List<KeywordRecommend.CandidateKeyword> commonCandidateKeywords, // 공통 후보군
            LocalDate targetDate) {

        if (user.getEmbedding() == null || commonCandidateKeywords.isEmpty()) {
            return List.of();
        }

        // 1. 유저의 기존 관심사 키워드 ID 추출
        Set<Long> userInterestKeywordIds = userInterestRepository.findByUserId(user.getId()).stream()
                .map(interest -> interest.getKeyword().getId())
                .collect(Collectors.toSet());

        // 2. 공통 후보군에 "유저 개인의 관심사 여부"를 덮어씌운 '개인화된 후보군' 생성
        List<KeywordRecommend.CandidateKeyword> personalizedCandidates = commonCandidateKeywords.stream()
                .map(candidate -> KeywordRecommend.CandidateKeyword.builder()
                        .keywordId(candidate.getKeywordId())
                        .word(candidate.getWord())
                        .normalizedWord(candidate.getNormalizedWord())
                        .embedding(candidate.getEmbedding())
                        .recentImportance(candidate.getRecentImportance())
                        .isUserInterest(userInterestKeywordIds.contains(candidate.getKeywordId())) // 🔥 여기서 매핑!
                        .build())
                .collect(Collectors.toList());

        // 3. AI 서버 요청 조립 (개인화된 후보군을 전달)
        KeywordRecommend.Request request = KeywordRecommend.Request.builder()
                .userId(user.getId())
                .userEmbedding(user.getEmbedding())
                .candidateKeywords(personalizedCandidates)
                .targetDate(targetDate)
                .topK(TOP_K_RECOMMENDATIONS)
                .build();

        KeywordRecommend.Response response = aiClient.recommendKeywords(request);

        recommendKeywordRepository.deleteByUserIdAndTargetDate(user.getId(), targetDate);

        List<RecommendKeyword> recommendEntities = response.getRecommendKeywords().stream()
                .map(res -> RecommendKeyword.create(
                        user,
                        keywordRepository.getReferenceById(res.getKeywordId()),
                        targetDate,
                        res.getScore()
                ))
                .collect(Collectors.toList());

        return recommendKeywordRepository.saveAll(recommendEntities);
    }
}
