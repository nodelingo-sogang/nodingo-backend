package nodingo.core.global.exception.news;

public class NewsNotFoundException extends RuntimeException {
    public NewsNotFoundException(String message) {
        super(message);
    }
}
