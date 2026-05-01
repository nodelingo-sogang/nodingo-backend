package nodingo.core.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InfoArticleWrapper {
    private NewsApiItem eng;
    private NewsApiItem kor;
}