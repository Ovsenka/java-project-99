package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthDTO {
    @NotBlank
    private String username;

    @NotNull
    @Size(min = 3)
    private String password;
}
