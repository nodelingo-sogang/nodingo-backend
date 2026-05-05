package nodingo.core.user.service.vector;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.userEmbedding.UserEmbedding;
import nodingo.core.global.exception.ai.AiIntegrationException;
import nodingo.core.global.exception.news.NewsNotFoundException;
import nodingo.core.global.exception.user.UserNotFoundException;
import nodingo.core.global.exception.userScrap.UserScrapNotFoundException;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.news.domain.News;
import nodingo.core.news.repository.NewsRepository;
import nodingo.core.user.domain.User;
import nodingo.core.user.domain.UserScrap;
import nodingo.core.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserVectorService {

    private final AiClient aiClient;
    private final UserRepository userRepository;
    private final NewsRepository newsRepository;

    @Transactional
    public void initUserEmbedding(User user, List<Keyword> selectedKeywords) {
        log.info(">>>> [UserVectorService] Starting initial embedding generation - userId: {}", user.getId());

        // 1. Assemble Request DTO for AI server
        List<UserEmbedding.InterestKeyword> keywordItems = selectedKeywords.stream()
                .map(keyword -> UserEmbedding.InterestKeyword.builder()
                        .keywordId(keyword.getId())
                        .word(keyword.getWord())
                        .embedding(keyword.getEmbedding())
                        .build())
                .collect(Collectors.toList());

        UserEmbedding.InitRequest request = UserEmbedding.InitRequest.builder()
                .userId(user.getId())
                .interestKeywords(keywordItems)
                .build();

        try {
            // 2. Call FastAPI server (v1/users/init-embedding)
            UserEmbedding.Response response = aiClient.initUserEmbedding(request);

            if (response == null || response.getEmbedding() == null) {
                throw new AiIntegrationException("Received empty embedding response from AI server.");
            }

            user.updateEmbedding(response.getEmbedding());

            log.info(">>>> [UserVectorService] Successfully generated initial embedding - userId: {}", user.getId());

        } catch (Exception e) {
            log.error(">>>> [UserVectorService] AI server communication failed - userId: {}, error: {}",
                    user.getId(), e.getMessage());

            throw new AiIntegrationException("Failed to initialize user embedding via AI server.");
        }
    }

    @Async("embeddingTaskExecutor")
    @Transactional
    public void updateUserEmbeddingAsync(Long userId, Long newsId, String type) {
        try {
            UserScrap scrap = getOrElseThrow(userId, newsId);

            User user = scrap.getUser();
            News news = scrap.getNews();

            // AI 업데이트
            UserEmbedding.Activity activity = UserEmbedding.Activity.createScrap(news, 0.5);
            UserEmbedding.UpdateRequest req = UserEmbedding.UpdateRequest.create(user, List.of(activity));
            UserEmbedding.Response res = aiClient.updateUserEmbedding(req);

            if (res != null && res.getEmbedding() != null) {
                user.updateEmbedding(res.getEmbedding());
                userRepository.saveAndFlush(user);
                log.info(">>>> [AI Success] User: {}", userId);
            }

        } catch (Exception e) {
            log.error(">>>> [AI Error] Message: {}", e.getMessage());
        }
    }

    private UserScrap getOrElseThrow(Long userId, Long newsId) {
        return newsRepository.findScrapDetail(userId, newsId)
                .orElseThrow(() -> new UserScrapNotFoundException("Scrap not found"));
    }
}