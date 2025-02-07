package hexlet.code.mapper;

import hexlet.code.model.BaseEntity;
import hexlet.code.model.Label;
import hexlet.code.model.Status;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.StatusRepository;
import jakarta.persistence.EntityManager;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public class ReferenceMapper {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private LabelRepository labelRepository;

    public <T extends BaseEntity> T toEntity(Long id, @TargetType Class<T> entityClass) {
        return id != null ? entityManager.find(entityClass, id) : null;
    }

    @Named("toStatusEntity")
    public Status toStatusEntity(String slug) {
        return statusRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Slug " + slug + " is not found"));
    }

    @Named("toLabelEntity")
    public Label toLabelEntity(Long labelID) {
        return labelRepository.findById(labelID)
                .orElseThrow(() -> new NoSuchElementException("Label with id " + labelID + " not found"));
    }

    @Named("toLabelEntities")
    public List<Label> toLabelEntities(List<Long> labelsIDs) {
        return labelsIDs == null || labelsIDs.isEmpty() ? new ArrayList<>() : labelsIDs.stream()
                .map(this::toLabelEntity)
                .toList();
    }

    @Named("toLabelNames")
    public List<Long> toLabelNames(List<Label> labels) {
        return labels == null || labels.isEmpty() ? new ArrayList<>() : labels.stream()
                .map(Label::getId)
                .toList();
    }
}
