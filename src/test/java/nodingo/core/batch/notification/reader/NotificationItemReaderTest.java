package nodingo.core.batch.notification.reader;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationItemReaderTest {

    @Mock
    private EntityManagerFactory entityManagerFactory;

    private NotificationItemReader reader;

    @BeforeEach
    void setUp() {
        reader = new NotificationItemReader(entityManagerFactory);
    }

    @Test
    @DisplayName("Reader 초기화 시 현재 시간 파라미터가 쿼리에 올바르게 매핑된다")
    void initTest() {
        // given
        int expectedHour = LocalDateTime.now().getHour();

        // when
        reader.init();

        // then
        assertThat(reader.getPageSize()).isEqualTo(100);

        Map<String, Object> params = (Map<String, Object>) ReflectionTestUtils.getField(reader, "parameterValues");

        assertThat(params).isNotNull();
        assertThat(params.get("hour")).isEqualTo(expectedHour); // 여기서 expectedHour를 사용하므로 경고가 사라집니다.

        System.out.println("검증된 시간: " + params.get("hour"));
    }
}