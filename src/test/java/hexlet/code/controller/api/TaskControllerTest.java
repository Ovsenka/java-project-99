package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.service.TaskService;
import hexlet.code.util.ModelsGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
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
class TaskControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ModelsGenerator generator;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TaskService service;

	private JwtRequestPostProcessor token;

	private Task task;

	@BeforeEach
	void prepare() {
		token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

		task = Instancio.of(generator.makeFakeTask()).create();

		taskRepository.save(task);
	}

	@Test
	void getAllTest() throws Exception {
		var anotherTask = Instancio.of(generator.makeFakeTask()).create();
		taskRepository.save(anotherTask);

		var request = get("/api/tasks").with(token);
		var response = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andReturn();
		var body = response.getResponse().getContentAsString();

		List<TaskDTO> actual = objectMapper.readValue(body, new TypeReference<>() { });

		assertThatJson(body).isArray();
		assertThat(actual.stream().allMatch(t -> taskRepository.findById(t.getId()).isPresent())).isTrue();
	}

	@Test
	void getAllFilterTest() throws Exception {
		var task1 = new TaskCreateDTO();
		task1.setAssigneeId(1L);
		task1.setTitle("Task One is finally in the house");
		task1.setContent("The description of the task One");
		task1.setStatus("to_be_fixed");
		task1.setTaskLabelIds(List.of(1L));

		var task2 = new TaskCreateDTO();
		task2.setAssigneeId(1L);
		task2.setTitle("Task Two has already in the house");
		task2.setContent("The description of the task Two");
		task2.setStatus("to_be_fixed");
		task2.setTaskLabelIds(List.of(1L));

		var savedOne = service.createTask(task1);
		var savedTwo = service.createTask(task2);
		List<Long> taskIds = new ArrayList<>();
		taskIds.add(savedOne.getId());
		taskIds.add(savedTwo.getId());

		var request = get("/api/tasks").with(token)
				.queryParam("titleCont", "in the house")
				.queryParam("assigneeId", "1")
				.queryParam("status", "to_be_fixed")
				.queryParam("labelId", "1");
		var response = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andReturn();
		var body = response.getResponse().getContentAsString();

		List<TaskDTO> actual = objectMapper.readValue(body, new TypeReference<>() { });

		assertThatJson(body).isArray();
		assertThat(actual.stream().allMatch(t -> taskIds.contains(t.getId()))).isTrue();
	}

	@Test
	void getByIdTest() throws Exception {
		long id  = task.getId();

		var request = get("/api/tasks/{id}", id).with(token);
		var response = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andReturn();
		var body = response.getResponse().getContentAsString();
		var testTask = taskRepository.findById(id);

		assertThat(testTask).isNotEmpty();
		assertThatJson(body).and(
				n -> n.node("title").isEqualTo(task.getName()),
				n -> n.node("content").isEqualTo(task.getDescription()),
				n -> n.node("id").isEqualTo(id)
		);
	}

	@Test
	void createTest() throws Exception {
		var dto = new TaskCreateDTO();
		dto.setTitle("yandex-name-create");
		dto.setContent("yandex-description-create");
		dto.setStatus("draft");
		dto.setTaskLabelIds(List.of(1L));

		var request = post("/api/tasks")
				.with(token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));
		var response = mockMvc.perform(request)
				.andExpect(status().isCreated())
				.andReturn();
		var body = response.getResponse().getContentAsString();
		var testTask = taskRepository.findByName("yandex-name-create").orElse(null);

		assertThat(testTask).isNotNull();
		assertThatJson(body).and(
				n -> n.node("title").isEqualTo(dto.getTitle()),
				n -> n.node("content").isEqualTo(dto.getContent()),
				n -> n.node("status").isEqualTo(dto.getStatus())
		);
	}


	@Test
	void createTestBadRequest() throws Exception {
		var dto = new TaskCreateDTO();
		dto.setTitle("");
		dto.setContent("yandex-description-create-failed");
		dto.setStatus("Draft");
		dto.setTaskLabelIds(List.of(1L));

		var request = post("/api/tasks")
				.with(token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));
		mockMvc.perform(request)
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateTest() throws Exception {
		long id  = task.getId();

		var dto = new TaskUpdateDTO();
		dto.setTitle(JsonNullable.of("yandex-name-update"));
		dto.setContent(JsonNullable.of("yandex-description-update"));
		dto.setStatus(JsonNullable.of("to_review"));

		var request = put("/api/tasks/{id}", id)
				.with(token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));
		var response = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andReturn();
		var body = response.getResponse().getContentAsString();

		assertThatJson(body).and(
				n -> n.node("title").isEqualTo(dto.getTitle().get()),
				n -> n.node("content").isEqualTo(dto.getContent().get()),
				n -> n.node("status").isEqualTo(dto.getStatus().get())
		);
		assertThat(taskRepository.findById(id).get().getName()).isEqualTo(dto.getTitle().get());
	}

	@Test
	void updateTestBadRequest() throws Exception {
		long id  = task.getId();

		var dto = new TaskUpdateDTO();
		dto.setTitle(JsonNullable.of(""));
		dto.setContent(JsonNullable.of("yandex-description-failed"));

		var request = put("/api/tasks/{id}", id)
				.with(token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));
		mockMvc.perform(request)
				.andExpect(status().isBadRequest());
	}

	@Test
	void destroyTest() throws Exception {
		long id  = task.getId();

		var request = delete("/api/tasks/{id}", id).with(token);
		mockMvc.perform(request)
				.andExpect(status().isNoContent());

		var maybeTask = taskRepository.findById(id).orElse(null);
		assertThat(maybeTask).isNull();
	}
}
