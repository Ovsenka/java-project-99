package hexlet.code.controller.api;

import hexlet.code.dto.status.StatusCreateDTO;
import hexlet.code.dto.status.StatusDTO;
import hexlet.code.dto.status.StatusUpdateDTO;
import hexlet.code.service.StatusService;
import hexlet.code.util.UserUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RestController
@RequestMapping("/api/task_statuses")
public class StatusController {

    @Autowired
    private StatusService service;

    @Autowired
    private UserUtils userUtils;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<StatusDTO>> getAll() {
        List<StatusDTO> statusDTOS = service.getAll();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(statusDTOS.size()));
        return ResponseEntity.ok()
                .headers(headers)
                .body(statusDTOS);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public StatusDTO getById(@PathVariable Long id) {
        return service.getStatusById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StatusDTO create(@Valid @RequestBody StatusCreateDTO dto) {
        return service.createStatus(dto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public StatusDTO update(@Valid @RequestBody StatusUpdateDTO dto, @PathVariable Long id) {
        return service.updateStatus(dto, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        service.destroyStatus(id);
    }
}
