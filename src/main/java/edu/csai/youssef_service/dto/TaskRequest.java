package edu.csai.youssef_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    private String status;
}

