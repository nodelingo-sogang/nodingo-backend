package nodingo.core.batch.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsApiItem {
    private String uri;
    private String lang;
    private String url;
    private String title;
    private String body;
    private String dateTimePub;
}
