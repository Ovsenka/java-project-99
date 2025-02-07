package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;

@Setter
@Getter
public class TaskUpdateDTO {
    @NotBlank
    @Size(min = 1)
    private JsonNullable<String> title;

    private JsonNullable<String> content;
    private JsonNullable<String> status;

    private JsonNullable<List<Long>> taskLabelIds;

    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;
}
