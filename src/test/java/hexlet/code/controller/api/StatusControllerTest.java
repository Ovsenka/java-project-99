package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.status.StatusCreateDTO;
import hexlet.code.dto.status.StatusDTO;
import hexlet.code.dto.status.StatusUpdateDTO;
import hexlet.code.model.Status;
import hexlet.code.repository.StatusRepository;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
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
class StatusControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelsGenerator generator;

	@Autowired
	private StatusRepository statusRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private JwtRequestPostProcessor token;

	private Status status;

	@BeforeEach
	void prepare() {
		token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

		status = Instancio.of(generator.getStatusModel()).create();

		statusRepository.save(status);
	}

	@AfterEach
	void cleanRepo() {
		statusRepository.deleteById(status.getId());
	}

	@Test
	void getAllTest() throws Exception {
		var request = get("/api/task_statuses").with(token);
		var response = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andReturn();
		var body = response.getResponse().getContentAsString();

		List<StatusDTO> actual = objectMapper.readValue(body, new TypeReference<>() { });

		assertThatJson(body).isArray();
		assertThat(actual.stream().allMatch(s -> statusRepository.findById(s.getId()).isPresent())).isTrue();
	}

	@Test
	void getByIdTest() throws Exception {
		long id  = status.getId();

		var request = get("/api/task_statuses/{id}", id).with(token);
		var response = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andReturn();
		var body = response.getResponse().getContentAsString();
		var testStatus = statusRepository.findById(id);

		assertThat(testStatus).isNotEmpty();
		assertThatJson(body).and(
				n -> n.node("name").isEqualTo(status.getName()),
				n -> n.node("slug").isEqualTo(status.getSlug()),
				n -> n.node("id").isEqualTo(id)
		);

	}

	@Test
	void createTest() throws Exception {
		var dto = new StatusCreateDTO();
		dto.setName("yandex-status-create-test");
		dto.setSlug("yandex-slug-create-test");

		var request = post("/api/task_statuses")
				.with(token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));
		var response = mockMvc.perform(request)
				.andExpect(status().isCreated())
				.andReturn();
		var body = response.getResponse().getContentAsString();
		var teststatus = statusRepository.findBySlug(dto.getSlug()).orElse(null);

		assertThat(teststatus).isNotNull();
		assertThatJson(body).and(
				n -> n.node("name").isEqualTo(dto.getName()),
				n -> n.node("slug").isEqualTo(dto.getSlug())
		);
	}

	@Test
	void createTestBadRequest() throws Exception {
		var dto = new StatusCreateDTO();
		dto.setName("");
		dto.setSlug("");

		var request = post("/api/task_statuses")
				.with(token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));
		mockMvc.perform(request)
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateTest() throws Exception {
		long id  = status.getId();

		var dto = new StatusUpdateDTO();
		dto.setName(JsonNullable.of("yandex-name-update"));
		dto.setSlug(JsonNullable.of("yandex-slug-update"));

		var request = put("/api/task_statuses/{id}", id)
				.with(token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));
		var response = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andReturn();
		var body = response.getResponse().getContentAsString();

		assertThatJson(body).and(
				n -> n.node("name").isEqualTo(dto.getName().get()),
				n -> n.node("slug").isEqualTo(dto.getSlug().get())
		);
		assertThat(statusRepository.findById(id).get().getName()).isEqualTo(dto.getName().get());
	}

	@Test
	void updateTestBadRequest() throws Exception {
		long id  = status.getId();

		var dto = new StatusUpdateDTO();
		dto.setName(JsonNullable.of(""));
		dto.setSlug(JsonNullable.of(""));

		var request = put("/api/task_statuses/{id}", id)
				.with(token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));
		mockMvc.perform(request)
				.andExpect(status().isBadRequest());
		assertThat(statusRepository.findById(id).get().getName()).isEqualTo(status.getName());
	}

	@Test
	void destroyTest() throws Exception {
		long id  = status.getId();

		var request = delete("/api/task_statuses/{id}", id).with(token);
		mockMvc.perform(request)
				.andExpect(status().isNoContent());

		var maybestatus = statusRepository.findById(id).orElse(null);
		assertThat(maybestatus).isNull();
	}
}
