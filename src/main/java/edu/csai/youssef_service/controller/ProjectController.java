package edu.csai.youssef_service.controller;

import edu.csai.youssef_service.dto.ProjectRequest;
import edu.csai.youssef_service.dto.ProjectResponse;
import edu.csai.youssef_service.dto.TaskRequest;
import edu.csai.youssef_service.dto.TaskResponse;
import edu.csai.youssef_service.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAll() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.createProjectWithTasks(request));
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<TaskResponse> addTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(projectService.addTaskToProject(id, request));
    }
}
