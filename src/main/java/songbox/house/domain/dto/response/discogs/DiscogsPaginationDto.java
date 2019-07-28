package songbox.house.domain.dto.response.discogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscogsPaginationDto {
    @JsonProperty("per_page")
    private Integer perPage;
    private Integer items;
    private Integer page;
    private Integer pages;
}
