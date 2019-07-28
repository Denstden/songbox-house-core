package songbox.house.service.impl;

import lombok.Data;
import org.springframework.stereotype.Component;
import songbox.house.service.TimeService;

import java.time.Clock;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;

@Component
@Data
public class TimeServiceImpl implements TimeService {

    private final Clock clock;

    @Override
    public long getNowSeconds() {
        return now(clock).toInstant(UTC).getEpochSecond();
    }
}
