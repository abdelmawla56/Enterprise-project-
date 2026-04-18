package edu.csai.youssef_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private Long tenantId;
    private List<TaskResponse> tasks;
}
