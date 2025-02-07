package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.AuthDTO;
import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import org.springframework.security.test.web.servlet
    .request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ModelsGenerator generator;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private JwtRequestPostProcessor token;

	private JwtRequestPostProcessor tokenUser;

	private User user;

	@BeforeEach
	void prepare() {
		token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
		user = Instancio.of(generator.getUserModel()).create();
		userRepository.save(user);
		tokenUser = jwt().jwt(builder -> builder.subject(user.getEmail()));
	}

	@AfterEach
	public void clean() {
		userRepository.deleteById(user.getId());
	}


	@Test
	void loginTest() throws Exception {
		var dto = new AuthDTO();
		dto.setPassword("noexist");
		dto.setUsername("noexist@google.com");
		token = jwt().jwt(builder -> builder.subject(dto.getUsername()));
		var request = post("/api/login")
				.with(token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));

		mockMvc.perform(request)
				.andExpect(status().isUnauthorized());

		assertThat(userRepository.findByEmail(dto.getUsername())).isEmpty();
	}

	@Test
	void getAllTest() throws Exception {
		var anotherUser = Instancio.of(generator.getUserModel()).create();
		userRepository.save(anotherUser);

		var request = get("/api/users").with(jwt());
		var response = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andReturn();
		var body = response.getResponse().getContentAsString();

		List<UserDTO> actual = objectMapper.readValue(body, new TypeReference<>() { });

		assertThatJson(body).isArray();
		assertThat(actual.stream().allMatch(u -> userRepository.findById(u.getId()).isPresent())).isTrue();
	}


	@Test
	void getByIdTest() throws Exception {
		long id  = user.getId();

		var request = get("/api/users/{id}", id).with(tokenUser);
		var response = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andReturn();
		var body = response.getResponse().getContentAsString();
		var testUser = userRepository.findById(id);

		assertThat(testUser).isNotEmpty();
		assertThatJson(body).and(
				n -> n.node("email").isEqualTo(user.getEmail()),
				n -> n.node("firstName").isEqualTo(user.getFirstName()),
				n -> n.node("lastName").isEqualTo(user.getLastName()),
				n -> n.node("id").isEqualTo(id)
		);
	}

	@Test
	void createTest() throws Exception {
		var dto = new UserCreateDTO();
		dto.setEmail("yandextestcreate@test.com");
		dto.setFirstName("yandexfirstName@test.com");
		dto.setLastName("yandexlastName@test.com");
		dto.setPassword("yandexPass");

		var request = post("/api/users")
				.with(token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));
		var response = mockMvc.perform(request)
				.andExpect(status().isCreated())
				.andReturn();
		var body = response.getResponse().getContentAsString();
		var testUser = userRepository.findByEmail(dto.getEmail()).orElse(null);

		assertThat(testUser).isNotNull();
		assertThatJson(body).and(
				n -> n.node("email").isEqualTo(dto.getEmail()),
				n -> n.node("firstName").isEqualTo(dto.getFirstName()),
				n -> n.node("lastName").isEqualTo(dto.getLastName())
		);
	}

	@Test
	void createTestBadRequest() throws Exception {
		var dto = new UserCreateDTO();
		dto.setEmail("yandextestcreate");
		dto.setFirstName("yandexfirstName@test.com");
		dto.setLastName("yandexlastName@test.com");
		dto.setPassword("yandexPass");

		var request = post("/api/users")
				.with(token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));
		mockMvc.perform(request)
				.andExpect(status().isBadRequest());
		var testUser = userRepository.findByEmail(dto.getEmail()).orElse(null);

		assertThat(testUser).isNull();
	}

	@Test
	void updateTest() throws Exception {
		long id  = user.getId();

		var dto = new UserUpdateDTO();
		dto.setEmail(JsonNullable.of("yandextestupdate@test.com"));
		dto.setFirstName(JsonNullable.of("nowaFirstName@test.com"));
		dto.setLastName(JsonNullable.of("nowaLastName@test.com"));

		var request = put("/api/users/{id}", id)
				.with(tokenUser)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto));
		var response = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andReturn();
		var body = response.getResponse().getContentAsString();

		assertThatJson(body).and(
				n -> n.node("email").isEqualTo(dto.getEmail().get()),
				n -> n.node("firstName").isEqualTo(dto.getFirstName().get()),
				n -> n.node("lastName").isEqualTo(dto.getLastName().get())
		);
		assertThat(userRepository.findById(id).get().getEmail()).isEqualTo(dto.getEmail().get());
	}


	@Test
	void destroyTest() throws Exception {
		long id  = user.getId();

		var request = delete("/api/users/{id}", id).with(tokenUser);
		mockMvc.perform(request)
				.andExpect(status().isNoContent());

		var maybeUser = userRepository.findById(id).orElse(null);
		assertThat(maybeUser).isNull();
	}

}
