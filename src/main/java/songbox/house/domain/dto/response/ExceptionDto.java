package songbox.house.domain.dto.response;

import lombok.Data;

@Data
public class ExceptionDto {
    private final String message;
    private final String exception;

    public ExceptionDto(Throwable throwable) {
        message = throwable.getMessage();
        exception = throwable.getClass().getSimpleName();
    }
}
