package edu.csai.youssef_service.service;

import org.springframework.context.ApplicationEvent;

/** Fired after a project is successfully persisted. Consumed by WorkflowEventListener. */
public class ProjectCreatedEvent extends ApplicationEvent {

    private final Long projectId;
    private final Long tenantId;

    public ProjectCreatedEvent(Object source, Long projectId, Long tenantId) {
        super(source);
        this.projectId = projectId;
        this.tenantId  = tenantId;
    }

    public Long getProjectId() { return projectId; }
    public Long getTenantId()  { return tenantId;  }
}
