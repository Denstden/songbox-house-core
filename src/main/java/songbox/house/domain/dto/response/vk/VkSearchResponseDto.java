package songbox.house.domain.dto.response.vk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VkSearchResponseDto {
    private List<ArrayList> list;
    private String nextOffset;
    private Boolean hasMore;

    @JsonIgnore
    private Boolean fromNews = false;
}
