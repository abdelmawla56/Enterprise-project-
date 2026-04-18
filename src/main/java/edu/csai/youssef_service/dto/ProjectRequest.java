package edu.csai.youssef_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ProjectRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    private List<TaskRequest> tasks;
}
