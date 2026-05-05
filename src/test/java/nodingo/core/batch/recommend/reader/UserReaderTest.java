package nodingo.core.batch.recommend.reader;

import nodingo.core.user.domain.User;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserReaderTest {

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @InjectMocks
    private UserReader userReaderConfig;

    @Test
    @DisplayName("User JpaPagingItemReader가 정상적인 쿼리와 설정으로 생성되어야 한다")
    void userReaderCreationTest() {
        // when
        JpaPagingItemReader<User> reader = userReaderConfig.userItemReader();

        // then
        assertThat(reader).isNotNull();

        String queryString = (String) ReflectionTestUtils.getField(reader, "queryString");
        Integer pageSize = (Integer) ReflectionTestUtils.getField(reader, "pageSize");

        assertThat(queryString).isEqualTo("SELECT u FROM User u WHERE u.embedding IS NOT NULL");
        assertThat(pageSize).isEqualTo(100);
    }
}