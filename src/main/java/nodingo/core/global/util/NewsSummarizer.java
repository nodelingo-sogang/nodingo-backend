package nodingo.core.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.news.domain.News;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsSummarizer {

    private final ChatClient chatClient;

    public String summarize(News news) {
        log.info(">>>> [LLM summary] NewsId: {} start summarizing", news.getId());

        try {
            return chatClient.prompt()
                    .system("당신은 뉴스 요약 전문가입니다. 뉴스 본문을 한국어 200자 내외로 핵심만 요약하세요.")
                    .user(u -> u.text("제목: {title}\n본문: {body}")
                            .param("title", news.getTitle())
                            .param("body", news.getBody()))
                    .call()
                    .content();
        } catch (Exception e) {
            log.error(">>>> [LLM summary failed] NewsId: {}, reason: {}", news.getId(), e.getMessage());
            return news.getBody().length() > 200
                    ? news.getBody().substring(0, 200) + "..."
                    : news.getBody();
        }
    }
}
