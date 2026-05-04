package nodingo.core.batch.news.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.batch.dto.event.EventApiItem;
import nodingo.core.batch.dto.event.EventApiResponse;
import nodingo.core.batch.service.NewsFetchService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class NewsApiReader implements ItemReader<EventApiItem> {

    private final NewsFetchService newsFetchService;

    private static final int MAX_TEST_PAGES = 2;
    private int currentPage = 1;
    private Iterator<EventApiItem> itemIterator = Collections.emptyIterator();
    private boolean isEnd = false;

    @Override
    public EventApiItem read() {
        // 1. 이미 읽어온 데이터가 Iterator에 남아있으면 바로 반환
        if (itemIterator.hasNext()) {
            return itemIterator.next();
        }

        // 2. 종료 조건 체크
        if (isEnd || currentPage > MAX_TEST_PAGES) {
            log.info(">>>> [Batch Reader] Finished or reached max test pages.");
            return null;
        }

        // 3. API 호출 (어제 자 뉴스 수집)
        LocalDate targetDate = LocalDate.now().minusDays(1);
        EventApiResponse response = newsFetchService.fetchEvents(targetDate, currentPage);

        // 4. 응답 검증
        if (response == null || response.getEvents() == null ||
                response.getEvents().getResults() == null || response.getEvents().getResults().isEmpty()) {
            log.warn(">>>> [Batch Reader] API response is empty. date: {}, page: {}", targetDate, currentPage);
            isEnd = true;
            return null;
        }

        // 5. Iterator 갱신 및 페이지 정보 계산
        List<EventApiItem> results = response.getEvents().getResults();
        itemIterator = results.iterator();

        int totalPages = response.getEvents().getPages() > 0 ? response.getEvents().getPages() : currentPage;
        int effectiveTotalPages = Math.min(totalPages, MAX_TEST_PAGES);

        log.info(">>>> [Batch Reader] Fetching Event page: {} / {} (total={}, items={})",
                currentPage, effectiveTotalPages, totalPages, results.size());

        if (currentPage >= effectiveTotalPages) {
            isEnd = true;
        }

        currentPage++;

        // 6. 새로 읽어온 데이터의 첫 번째 아이템 반환
        return itemIterator.hasNext() ? itemIterator.next() : null;
    }
}
