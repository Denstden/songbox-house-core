package songbox.house.domain.dto.response;

import lombok.Data;

@Data
public class PageDto {
    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalPages;
    private Long totalElements;
}
