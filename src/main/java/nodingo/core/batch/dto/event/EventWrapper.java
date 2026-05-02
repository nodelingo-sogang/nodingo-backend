package nodingo.core.batch.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventWrapper {
    @JsonProperty("results")
    private List<EventApiItem> results;

    private int totalResults;
    private int pages;
    private int count;
}