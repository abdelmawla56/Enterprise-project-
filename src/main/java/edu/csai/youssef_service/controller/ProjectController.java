package edu.csai.youssef_service.controller;

import edu.csai.youssef_service.dto.ProjectRequest;
import edu.csai.youssef_service.dto.ProjectResponse;
import edu.csai.youssef_service.dto.TaskRequest;
import edu.csai.youssef_service.dto.TaskResponse;
import edu.csai.youssef_service.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    // Both roles can list / read projects
    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','TENANT_USER')")
    public ResponseEntity<List<ProjectResponse>> getAll() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','TENANT_USER')")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    // Only admins may create projects (→ 403 for TENANT_USER)
    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProjectWithTasks(request));
    }

    // Only admins may add tasks
    @PostMapping("/{id}/tasks")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<TaskResponse> addTask(@PathVariable Long id,
                                                @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.addTaskToProject(id, request));
    }
}


