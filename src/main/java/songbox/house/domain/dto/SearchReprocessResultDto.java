package songbox.house.domain.dto;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import songbox.house.domain.dto.response.TrackMetadataDto;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(level = PRIVATE)
public class SearchReprocessResultDto {
    Long ownerId;
    Long collectionId;
    Set<String> genres;
    TrackMetadataDto trackMetadata;
}
