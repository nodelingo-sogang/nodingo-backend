package nodingo.core.user.service.vector;

import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.userEmbedding.UserEmbedding;
import nodingo.core.global.exception.ai.AiIntegrationException;
import nodingo.core.keyword.domain.Keyword;
import nodingo.core.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class UserVectorServiceTest {

    @InjectMocks
    private UserVectorService userVectorService;

    @Mock
    private AiClient aiClient;

    @Test
    @DisplayName("User initial embedding initialization success")
    void initUserEmbedding_Success() {
        // 1. Given
        User user = User.create("google", "sub-1", "testUser", "Tester", "test@test.com");
        Keyword k1 = Keyword.create("AI");
        Keyword k2 = Keyword.create("Java");

        // Mock embeddings for keywords
        float[] mockKeywordVector = new float[1024];
        mockKeywordVector[0] = 0.5f;
        k1.updateEmbedding(mockKeywordVector);
        k2.updateEmbedding(mockKeywordVector);

        float[] mockUserVector = new float[1024];
        mockUserVector[0] = 0.9f;

        UserEmbedding.Response mockResponse = new UserEmbedding.Response(1L, mockUserVector);

        given(aiClient.initUserEmbedding(any(UserEmbedding.InitRequest.class)))
                .willReturn(mockResponse);

        // 2. When
        userVectorService.initUserEmbedding(user, List.of(k1, k2));

        // 3. Then
        assertThat(user.getEmbedding()).isEqualTo(mockUserVector);
        verify(aiClient, times(1)).initUserEmbedding(any());
    }

    @Test
    @DisplayName("Throws AiIntegrationException when AI server returns null")
    void initUserEmbedding_ServerFailure() {
        // Given
        User user = User.create("google", "sub-1", "testUser", "Tester", "test@test.com");
        given(aiClient.initUserEmbedding(any())).willThrow(new RuntimeException("Connection Failed"));

        // When & Then
        assertThrows(AiIntegrationException.class, () -> {
            userVectorService.initUserEmbedding(user, List.of());
        });
    }
}