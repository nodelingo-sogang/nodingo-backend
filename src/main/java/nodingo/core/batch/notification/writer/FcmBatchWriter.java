package nodingo.core.batch.notification.writer;

import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.notification.service.command.FcmService;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmBatchWriter implements ItemWriter<Message> {

    private final FcmService fcmService;

    @Override
    public void write(Chunk<? extends Message> chunk) {
        List<? extends Message> items = chunk.getItems();
        log.info("Batch writing {} FCM messages", items.size());
        if (!items.isEmpty()) {
            List<Message> messages = new ArrayList<>(items);
            fcmService.sendMessages(messages);
        }
    }
}
