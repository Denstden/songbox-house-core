package songbox.house.domain.event;

import org.springframework.context.ApplicationEvent;

public class GoogleRefreshTokenEvent extends ApplicationEvent {

    private final Long userId;
    private final String accessToken;

    public GoogleRefreshTokenEvent(Object source, Long userId, String accessToken) {
        super(source);
        this.userId = userId;
        this.accessToken = accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
