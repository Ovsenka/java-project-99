package hexlet.code.component;

import hexlet.code.mapper.UserMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Status;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.StatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.LabelService;
import hexlet.code.service.StatusService;
import hexlet.code.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
public class InitData implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private LabelService labelService;

    @Autowired
    private LabelRepository labelRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initUser();
        initStatuses();
        initLabels();
    }

    private void initUser() {
        User user = new User();
        user.setEmail("hexlet@example.com");
        user.setPassword("qwerty");
        var isUserPresent = userRepository.findByEmail(user.getEmail()).isEmpty();
        if (isUserPresent) {
            userRepository.save(user);
        }
        var hexletUser = userRepository.findByEmail("hexlet@example.com").get();
        System.out.println("Init user: " + hexletUser + " created");
    }

    private void initStatuses() {
        List<String> slugs = List.of("draft", "to_review", "to_be_fixed", "to_publish", "published");
        List<String> titles = List.of("Draft", "To Review", "To Be Fixed", "To Publish", "Published");
        List<Status> statusList = IntStream.range(0, titles.size())
                        .mapToObj(i -> {
                            var item = new Status();
                            item.setName(titles.get(i));
                            item.setSlug(slugs.get(i));
                            return item;
                        }).toList();

        statusList.forEach(s -> {
            if (statusRepository.findBySlug(s.getSlug()).isEmpty()) {
                statusRepository.save(s);
            }
        });
        List<String> checkTitles = statusRepository.findAll().stream()
                .map(Status::getName)
                .toList();
        System.out.println("Init statuses: " + checkTitles + " created");
    }

    private void initLabels() {
        List<String> labels = List.of("feature", "bug");
        List<Label> labelList = labels.stream().map(label -> {
            var item = new Label();
            item.setName(label);
            return item;
        }).toList();
        labelList.forEach(l -> {
            if (labelRepository.findByName(l.getName()).isEmpty()) {
                labelRepository.save(l);
            }
        });
        var checkLables = labelRepository.findAll().stream()
                .map(Label::getName)
                .toList();
        System.out.println("Init labels: " + checkLables + " created");
    }
}
