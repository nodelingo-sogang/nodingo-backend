package nodingo.core.batch.notification.writer;

import com.google.firebase.messaging.Message;
import nodingo.core.notification.service.command.FcmService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FcmBatchWriterTest {

    @Mock
    private FcmService fcmService;

    @InjectMocks
    private FcmBatchWriter fcmBatchWriter;

    @Test
    @DisplayName("Chunk에 담긴 메시지 리스트를 FcmService의 sendMessages로 그대로 전달한다")
    void fcmBatchWriterTest() throws Exception {
        // given
        List<Message> messages = List.of(
                Message.builder().setToken("token-1").build(),
                Message.builder().setToken("token-2").build()
        );
        Chunk<Message> chunk = new Chunk<>(messages);

        // when
        fcmBatchWriter.write(chunk);

        // then
        verify(fcmService, times(1)).sendMessages(anyList());
    }

    @Test
    @DisplayName("빈 Chunk가 들어왔을 때 Writer의 로그 및 서비스 호출 흐름을 검증한다")
    void writeEmptyChunkTest() throws Exception {
        // given
        Chunk<Message> emptyChunk = new Chunk<>(List.of());

        // when
        fcmBatchWriter.write(emptyChunk);

        // then
        verify(fcmService, times(0)).sendMessages(anyList());
    }
}