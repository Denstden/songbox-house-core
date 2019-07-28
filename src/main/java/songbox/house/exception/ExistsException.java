package songbox.house.exception;

public class ExistsException extends RuntimeException {
    public ExistsException(String message) {
        super(message);
    }
}
