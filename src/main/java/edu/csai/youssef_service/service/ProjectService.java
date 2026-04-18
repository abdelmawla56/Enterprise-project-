package edu.csai.youssef_service.service;

import edu.csai.youssef_service.dto.ProjectRequest;
import edu.csai.youssef_service.dto.ProjectResponse;
import edu.csai.youssef_service.dto.TaskRequest;
import edu.csai.youssef_service.dto.TaskResponse;
import edu.csai.youssef_service.entity.Project;
import edu.csai.youssef_service.entity.Task;
import edu.csai.youssef_service.multitenancy.TenantContext;
import edu.csai.youssef_service.repository.ProjectRepository;
import edu.csai.youssef_service.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        return mapToResponse(project);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProjectResponse createProjectWithTasks(ProjectRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        String createdBy = SecurityContextHolder.getContext().getAuthentication().getName();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(createdBy)
                .build();
        project.setTenantId(tenantId);

        Project savedProject = projectRepository.save(project);

        if (request.getTasks() != null) {
            for (TaskRequest taskReq : request.getTasks()) {
                if ("FAIL_TRANSACTION".equals(taskReq.getTitle())) {
                    throw new RuntimeException("Intentional failure to rollback transaction");
                }

                Task task = Task.builder()
                        .title(taskReq.getTitle())
                        .description(taskReq.getDescription())
                        .status(taskReq.getStatus() != null ? taskReq.getStatus() : "TODO")
                        .project(savedProject)
                        .build();
                task.setTenantId(tenantId);
                taskRepository.save(task);
            }
        }

        return mapToResponse(savedProject);
    }

    @Transactional
    public TaskResponse addTaskToProject(Long projectId, TaskRequest request) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "TODO")
                .project(project)
                .build();
        task.setTenantId(TenantContext.getCurrentTenant());
        Task savedTask = taskRepository.save(task);
        return mapTaskToResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        return mapTaskToResponse(taskRepository.save(task));
    }

    private ProjectResponse mapToResponse(Project project) {
        List<TaskResponse> taskResponses = project.getTasks() != null ? 
                project.getTasks().stream().map(this::mapTaskToResponse).collect(Collectors.toList()) : List.of();

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
