package hexlet.code.specification;

import hexlet.code.dto.task.TaskFilterDTO;
import hexlet.code.model.Task;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecification {
    public Specification<Task> build(TaskFilterDTO params) {
        return withTitleCont(params.getTitleCont())
                .and(withAssigneeId(params.getAssigneeId()))
                .and(withSlug(params.getStatus()))
                .and(withLabelId(params.getLabelId()));
    }

    public Specification<Task> withTitleCont(String titleCont) {
        return (root, query, cb) -> titleCont == null || titleCont.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("name")), "%" + titleCont.toLowerCase().trim() + "%");
    }

    public Specification<Task> withAssigneeId(Long assigneeId) {
        return (root, query, cb) -> assigneeId == null
                ? cb.conjunction()
                : cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    public Specification<Task> withSlug(String slug) {
        return (root, query, cb) -> slug == null || slug.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("taskStatus").get("slug"), slug);
    }

    public Specification<Task> withLabelId(Long labelId) {
        return (root, query, cb) -> labelId == null
                ? cb.conjunction()
                : cb.equal(root.join("labels", JoinType.LEFT).get("id"), labelId);
    }
}
