package edu.csai.youssef_service.service;

import org.springframework.context.ApplicationEvent;

/** Fired after a task is successfully persisted. Consumed by WorkflowEventListener. */
public class TaskCreatedEvent extends ApplicationEvent {

    private final Long taskId;
    private final Long projectId;
    private final Long tenantId;

    public TaskCreatedEvent(Object source, Long taskId, Long projectId, Long tenantId) {
        super(source);
        this.taskId    = taskId;
        this.projectId = projectId;
        this.tenantId  = tenantId;
    }

    public Long getTaskId()    { return taskId;    }
    public Long getProjectId() { return projectId; }
    public Long getTenantId()  { return tenantId;  }
}
