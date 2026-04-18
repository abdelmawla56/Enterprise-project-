package edu.csai.youssef_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String roles;
    private Long tenantId;
}
