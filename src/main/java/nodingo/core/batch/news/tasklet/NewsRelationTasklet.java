package nodingo.core.batch.news.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nodingo.core.ai.client.AiClient;
import nodingo.core.ai.dto.relation.NewsRelationAnalysis;
import nodingo.core.news.domain.News;
import nodingo.core.news.domain.NewsRelation;
import nodingo.core.news.repository.NewsRelationRepository;
import nodingo.core.news.repository.NewsRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsRelationTasklet implements Tasklet {

    private final NewsRepository newsRepository;
    private final NewsRelationRepository newsRelationRepository;
    private final AiClient aiClient;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info(">>>> [Relation Tasklet] 뉴스 관계 형성 프로세스 시작");

        // 1. 분석 대상 뉴스 조회 (임베딩 필수)
        List<News> targetNews = newsRepository.findAll().stream()
                .filter(n -> n.getEmbedding() != null)
                .toList();

        if (targetNews.size() < 2) {
            log.warn(">>>> [Relation Tasklet] 비교할 뉴스가 부족합니다. (건수: {})", targetNews.size());
            return RepeatStatus.FINISHED;
        }

        // 2. AI 서버 요청 객체 생성
        List<NewsRelationAnalysis.NewsEmbeddingInput> inputs = targetNews.stream()
                .map(n -> NewsRelationAnalysis.NewsEmbeddingInput.builder()
                        .newsId(n.getId())
                        .embedding(n.getEmbedding())
                        .build())
                .toList();

        NewsRelationAnalysis.Request request = NewsRelationAnalysis.Request.builder()
                .newsEmbeddings(inputs)
                .similarityThreshold(0.7)
                .build();

        try {
            // 3. FastAPI 호출
            NewsRelationAnalysis.Response response = aiClient.buildNewsRelations(request);

            // 4. 결과 매핑 및 DB 저장
            if (response.getRelations() != null) {
                for (NewsRelationAnalysis.RelationResult res : response.getRelations()) {
                    News subject = newsRepository.findById(res.getSubjectNewsId()).orElse(null);
                    News related = newsRepository.findById(res.getRelatedNewsId()).orElse(null);

                    if (subject != null && related != null) {
                        // 중복 방지 로직이 포함된 엔티티 생성
                        NewsRelation relation = NewsRelation.create(subject, related, res.getRelationScore());
                        newsRelationRepository.save(relation);
                    }
                }
                log.info(">>>> [Relation Tasklet] 성공적으로 {}개의 관계를 형성했습니다.", response.getRelations().size());
            }

        } catch (Exception e) {
            log.error(">>>> [Relation Tasklet] AI 서버 통신 에러: {}", e.getMessage());
        }

        return RepeatStatus.FINISHED;
    }
}
