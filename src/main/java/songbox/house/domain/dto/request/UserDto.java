package songbox.house.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UserDto {
    private final String userName;
    private final String password;
    private final String email;
    private final String name;
    private final String surname;
}
