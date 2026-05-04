package nodingo.core.batch.news.reader;

import nodingo.core.batch.dto.event.EventApiItem;
import nodingo.core.batch.dto.event.EventApiResponse;
import nodingo.core.batch.dto.event.EventWrapper;
import nodingo.core.batch.service.NewsFetchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NewsApiReaderTest {

    @Mock private NewsFetchService newsFetchService;
    @InjectMocks private NewsApiReader reader;

    @Test
    @DisplayName("API로부터 받은 데이터를 Iterator를 통해 순차적으로 읽어야 한다")
    void read_Success() throws Exception {
        // given
        EventApiItem eventItem = new EventApiItem();

        ReflectionTestUtils.setField(eventItem, "uri", "event-1");

        EventWrapper wrapper = new EventWrapper();
        ReflectionTestUtils.setField(wrapper, "results", List.of(eventItem));
        ReflectionTestUtils.setField(wrapper, "pages", 1);

        EventApiResponse response = new EventApiResponse();
        ReflectionTestUtils.setField(response, "events", wrapper);

        given(newsFetchService.fetchEvents(any(LocalDate.class), eq(1)))
                .willReturn(response);

        // when
        EventApiItem result = reader.read();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUri()).isEqualTo("event-1");
    }
}