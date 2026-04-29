package nodingo.core.global.config.news;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NewsApiConfig {

    @Value("${news.api.key}")
    private String apiKey;

    @Value("${news.api.base-url}")
    private String baseUrl;

    @Bean
    public RestClient newsApiClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
