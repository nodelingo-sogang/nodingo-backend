package nodingo.core.user.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewsScrapCommand {
    private final Long userId;
    private final Long newsId;

    public static NewsScrapCommand of(Long userId, Long newsId) {
        return new NewsScrapCommand(userId, newsId);
    }
}