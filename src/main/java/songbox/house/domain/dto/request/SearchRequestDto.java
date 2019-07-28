package songbox.house.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchRequestDto {
    private String artists;
    private String title;
    private Set<String> genres;
    private Long collectionId;
    private boolean only320 = false;
}
