package edu.csai.youssef_service.controller;

import edu.csai.youssef_service.dto.TaskRequest;
import edu.csai.youssef_service.dto.TaskResponse;
import edu.csai.youssef_service.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ProjectService projectService;

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        return ResponseEntity.ok(projectService.updateTask(id, request));
    }
}

