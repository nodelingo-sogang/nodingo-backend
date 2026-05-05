package nodingo.core.user.service.vector;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.userEmbedding.UserEmbedding;
import nodingo.core.global.exception.ai.AiIntegrationException;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserVectorService {

    private final AiClient aiClient;

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


}