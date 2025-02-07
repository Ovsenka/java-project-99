package hexlet.code.util;

import hexlet.code.model.Label;
import hexlet.code.model.Status;
import hexlet.code.model.Task;
import hexlet.code.model.User;
import hexlet.code.repository.StatusRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ModelsGenerator {
    @Autowired
    private Faker faker;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StatusRepository statusRepository;

    private Model<User> userModel;
    private Model<Status> statusModel;
    private Model<Task> taskModel;
    private Model<Label> labelModel;

    @PostConstruct
    public void initData() {
        userModel =  makeFakeUser();

        statusModel = makeFakeStatus();

        taskModel = makeFakeTask();

        labelModel = makeFakeLabel();
    }

    public Model<User> makeFakeUser() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getPassword), () -> passwordEncoder.encode("password"))
                .toModel();
    }

    public Model<Status> makeFakeStatus() {
        return Instancio.of(Status.class)
                .ignore(Select.field(Status::getId))
                .supply(Select.field(Status::getName), () -> faker.name().name())
                .supply(Select.field(Status::getSlug), () -> faker.hobbit().character())
                .toModel();
    }

    public Model<Task> makeFakeTask() {
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getAssignee))
                .supply(Select.field(Task::getName), () -> faker.funnyName().name())
                .supply(Select.field(Task::getDescription), () -> faker.esports().game())
                .supply(Select.field(Task::getTaskStatus), () -> statusRepository.findBySlug("draft").get())
                .supply(Select.field(Task::getIndex), () -> Integer.valueOf(faker.number().digit()))
                .ignore(Select.field(Task::getLabels))
                .toModel();
    }

    public Model<Label> makeFakeLabel() {
        return Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .supply(Select.field(Label::getName), () -> faker.funnyName().name())
                .toModel();
    }
}
