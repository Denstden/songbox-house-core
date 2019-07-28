package songbox.house.exception;

public class NotExistsException extends RuntimeException {
    public NotExistsException(final String message) {
        super(message);
    }
}
