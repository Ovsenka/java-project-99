package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.util.ModelsGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelsGenerator generator;

    @Autowired
    private LabelRepository repository;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private Label label;

    @BeforeEach
    void prepare() {

        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

        label = Instancio.of(generator.getLabelModel()).create();

        repository.save(label);
    }

    @AfterEach
    void cleanRepo() {
        repository.deleteById(label.getId());
    }

    @Test
    void getAllTest() throws Exception {
        var anotherLabel = Instancio.of(generator.getLabelModel()).create();
        repository.save(anotherLabel);

        var request = get("/api/labels")
                .with(token);
        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = response.getResponse().getContentAsString();

        List<LabelDTO> actual = objectMapper.readValue(body, new TypeReference<>() { });
        assertThat(actual.stream().allMatch(l -> repository.findById(l.getId()).isPresent())).isTrue();
        assertThatJson(body).isArray();
    }

    @Test
    void getByIdTest() throws Exception {
        long id = label.getId();

        var request = get("/api/labels/{id}", id)
                .with(token);
        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = response.getResponse().getContentAsString();

        var testLabel = repository.findById(id);

        assertThat(testLabel).isNotEmpty();
        assertThatJson(body).and(
                n -> n.node("name").isEqualTo(label.getName()),
                n -> n.node("id").isEqualTo(id)

        );
    }

    @Test
    void createTest() throws Exception {
        var dto = new LabelCreateDTO();
        dto.setName("yandex-name-label");

        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();
        var body = response.getResponse().getContentAsString();

        var maybeLabel = repository.findByName(dto.getName()).orElse(null);

        assertThat(maybeLabel).isNotNull();
        assertThatJson(body).and(
                n -> n.node(("name")).isEqualTo(dto.getName())
        );
    }

    @Test
    void createTestBadRequest() throws Exception {
        var dto = new LabelCreateDTO();
        dto.setName("12");

        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        var maybeLabel = repository.findByName(dto.getName()).orElse(null);

        assertThat(maybeLabel).isNull();
    }

    @Test
    void updateTest() throws Exception {
        long id = label.getId();

        var dto = new LabelUpdateDTO();
        dto.setName(JsonNullable.of("yandex-name-label-update"));

        var request = put("/api/labels/{id}", id)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = response.getResponse().getContentAsString();

        assertThat(repository.findByName(dto.getName().get())).isNotEqualTo(label.getName());

        assertThatJson(body).and(
                n -> n.node("name").isEqualTo(dto.getName())
        );
    }

    @Test
    void updateTestBadRequest() throws Exception {
        long id = label.getId();

        var dto = new LabelUpdateDTO();
        dto.setName(JsonNullable.of("yy"));

        var request = put("/api/labels/{id}", id)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        var maybeLabel = repository.findById(id).get();
        var failedLabel = repository.findByName(dto.getName().get()).orElse(null);

        assertThat(maybeLabel.getName()).isEqualTo(label.getName());
        assertThat(failedLabel).isNull();
    }

    @Test
    void destroyTest() throws Exception {
        long id = label.getId();

        var request = delete("/api/labels/{id}", id)
                .with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var maybeLabel = repository.findById(id).orElse(null);
        assertThat(maybeLabel).isNull();
    }

}
