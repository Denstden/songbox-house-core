package songbox.house.event;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.context.ApplicationEvent;
import songbox.house.domain.dto.SearchReprocessResultDto;

import java.util.Map;

public class SearchReprocessFoundEvent extends ApplicationEvent {

    private final Long userId;
    private final Map<Long, SearchReprocessResultDto> reprocessResultIdToReprocessResultMap;

    public SearchReprocessFoundEvent(Object source, Long userId,
            Map<Long, SearchReprocessResultDto> reprocessResultIdToReprocessResultMap) {
        super(source);
        this.userId = userId;
        this.reprocessResultIdToReprocessResultMap = reprocessResultIdToReprocessResultMap;
    }

    public Long getUserId() {
        return userId;
    }

    public Map<Long, SearchReprocessResultDto> getReprocessResultIdToReprocessResultMap() {
        return reprocessResultIdToReprocessResultMap;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userId", userId)
                .append("reprocessResultIdToReprocessResultMap", reprocessResultIdToReprocessResultMap)
                .toString();
    }
}
