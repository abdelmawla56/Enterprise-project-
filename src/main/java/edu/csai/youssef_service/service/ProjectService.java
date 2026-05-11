package edu.csai.youssef_service.service;

import edu.csai.youssef_service.dto.ProjectRequest;
import edu.csai.youssef_service.dto.ProjectResponse;
import edu.csai.youssef_service.dto.TaskRequest;
import edu.csai.youssef_service.dto.TaskResponse;
import edu.csai.youssef_service.entity.Project;
import edu.csai.youssef_service.entity.Task;
import edu.csai.youssef_service.exception.TenantAccessException;
import edu.csai.youssef_service.multitenancy.TenantContext;
import edu.csai.youssef_service.repository.ProjectRepository;
import edu.csai.youssef_service.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    @Autowired private ProjectRepository projectRepository;
    @Autowired private TaskRepository    taskRepository;
    @Autowired private ApplicationEventPublisher eventPublisher;

    // ── Helpers ──────────────────────────────────────────────────────────

    /** Defence-in-depth: verify the resource belongs to the current tenant. */
    private void assertTenantOwns(Long resourceTenantId) {
        Long currentTenant = TenantContext.getCurrentTenant();
        if (!Objects.equals(resourceTenantId, currentTenant)) {
            log.warn("TENANT ISOLATION BREACH ATTEMPT: tenant={} tried to access resource of tenant={}",
                    currentTenant, resourceTenantId);
            throw new TenantAccessException(
                    "Access denied: resource does not belong to your tenant");
        }
    }

    // ── Read ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        // Hibernate @Filter (via TenantFilterAspect) already restricts the query.
        return projectRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found: " + id));
        // Defence-in-depth check
        assertTenantOwns(project.getTenantId());
        return mapToResponse(project);
    }

    // ── Write ─────────────────────────────────────────────────────────────

    @Transactional(rollbackFor = Exception.class)
    public ProjectResponse createProjectWithTasks(ProjectRequest request) {
        Long tenantId  = TenantContext.getCurrentTenant();
        String creator = SecurityContextHolder.getContext().getAuthentication().getName();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(creator)
                .build();
        project.setTenantId(tenantId);
        Project saved = projectRepository.save(project);

        if (request.getTasks() != null) {
            for (TaskRequest taskReq : request.getTasks()) {
                if ("FAIL_TRANSACTION".equals(taskReq.getTitle())) {
                    throw new RuntimeException("Intentional failure to test transaction rollback");
                }
                Task task = Task.builder()
                        .title(taskReq.getTitle())
                        .description(taskReq.getDescription())
                        .status(taskReq.getStatus() != null ? taskReq.getStatus() : "TODO")
                        .project(saved)
                        .build();
                task.setTenantId(tenantId);
                taskRepository.save(task);
            }
        }

        log.info("Project created: id={} tenant={} by={}", saved.getId(), tenantId, creator);

        // Publish async event for messaging consumers
        eventPublisher.publishEvent(new ProjectCreatedEvent(this, saved.getId(), tenantId));

        return mapToResponse(saved);
    }

    @Transactional
    public TaskResponse addTaskToProject(Long projectId, TaskRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
        assertTenantOwns(project.getTenantId());

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "TODO")
                .project(project)
                .build();
        task.setTenantId(TenantContext.getCurrentTenant());
        Task saved = taskRepository.save(task);

        log.info("Task added: taskId={} projectId={} tenant={}", saved.getId(), projectId, task.getTenantId());
        eventPublisher.publishEvent(new TaskCreatedEvent(this, saved.getId(), projectId, task.getTenantId()));

        return mapTaskToResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));
        assertTenantOwns(task.getTenantId());

        if (request.getTitle()       != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus()      != null) task.setStatus(request.getStatus());
        return mapTaskToResponse(taskRepository.save(task));
    }

    // ── Mappers ───────────────────────────────────────────────────────────

    private ProjectResponse mapToResponse(Project project) {
        List<TaskResponse> taskResponses = project.getTasks() != null
                ? project.getTasks().stream().map(this::mapTaskToResponse).collect(Collectors.toList())
                : List.of();
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .tenantId(project.getTenantId())
                .tasks(taskResponses)
                .build();
    }

    private TaskResponse mapTaskToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .projectId(task.getProject().getId())
                .tenantId(task.getTenantId())
                .build();
    }
}


