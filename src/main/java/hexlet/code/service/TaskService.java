package hexlet.code.service;

import hexlet.code.specification.TaskSpecification;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskFilterDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TaskService {
    @Autowired
    private TaskRepository repository;

    @Autowired
    private TaskMapper mapper;

    @Autowired
    private TaskSpecification specification;

    public List<TaskDTO> getAll(TaskFilterDTO filterDTO) {
        var spec = specification.build(filterDTO);
        var filteredTasks = repository.findAll(spec);
        return filteredTasks.stream().map(mapper::map).toList();
    }

    public TaskDTO getTaskById(long id) {
        var maybeTask =  repository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        return mapper.map(maybeTask);
    }

    public TaskDTO createTask(TaskCreateDTO dto) {
        var task = mapper.map(dto);
        repository.save(task);
        return mapper.map(task);
    }

    public TaskDTO updateTask(TaskUpdateDTO dto, long id) {
        var maybeTask =  repository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        mapper.update(dto, maybeTask);
        repository.save(maybeTask);
        return mapper.map(maybeTask);
    }

    public void destroyTask(long id) {
        repository.deleteById(id);
    }
}
