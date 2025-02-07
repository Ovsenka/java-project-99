package hexlet.code.mapper;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.model.User;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeforeMapping;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
        uses = {JsonNullableMapper.class},
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.WARN
)
public abstract class UserMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public abstract User map(UserDTO dto);

    public abstract UserDTO map(User user);

    public abstract User map(UserCreateDTO dto);

    public abstract void update(UserUpdateDTO dto, @MappingTarget User user);

    @BeforeMapping // Marks a method to be invoked at the beginning of a generated mapping method.
    public void toHashPass(UserCreateDTO dto) {
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            var encodedPass = passwordEncoder.encode(dto.getPassword());
            dto.setPassword(encodedPass);
        }
    }

    @BeforeMapping
    public void toHashPass(UserUpdateDTO dto) {
        if (dto.getPassword() != null && dto.getPassword().isPresent()) {
            var encodedPass = passwordEncoder.encode(dto.getPassword().get());
            dto.setPassword(JsonNullable.of(encodedPass));
        }
    }
}
