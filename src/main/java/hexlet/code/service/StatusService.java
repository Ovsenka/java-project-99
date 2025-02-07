package hexlet.code.service;

import hexlet.code.dto.status.StatusCreateDTO;
import hexlet.code.dto.status.StatusDTO;
import hexlet.code.dto.status.StatusUpdateDTO;
import hexlet.code.mapper.StatusMapper;
import hexlet.code.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StatusService {
    @Autowired
    private StatusRepository repository;

    @Autowired
    private StatusMapper mapper;

    public List<StatusDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::map)
                .toList();
    }

    public StatusDTO getStatusById(long id) {
        var maybeStatus =  repository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        return mapper.map(maybeStatus);
    }

    public StatusDTO createStatus(StatusCreateDTO dto) {
        var status = mapper.map(dto);
        repository.save(status);
        return mapper.map(status);
    }

    public StatusDTO updateStatus(StatusUpdateDTO dto, long id) {
        var maybeStatus =  repository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        mapper.update(dto, maybeStatus);
        repository.save(maybeStatus);
        return mapper.map(maybeStatus);
    }

    public void destroyStatus(long id) {
        repository.deleteById(id);
    }
}
