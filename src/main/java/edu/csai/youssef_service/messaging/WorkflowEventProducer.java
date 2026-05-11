package edu.csai.youssef_service.messaging;

import edu.csai.youssef_service.service.ProjectCreatedEvent;
import edu.csai.youssef_service.service.TaskCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Producer — listens for Spring application events (fired inside a DB transaction)
 * and forwards them to RabbitMQ AFTER the transaction commits.
 *
 * The @Async annotation ensures the main request thread is never blocked by the
 * broker call. If the broker is unavailable the application still starts and
 * serves HTTP requests normally; only the async publish fails (logged as WARN).
 */
@Component
public class WorkflowEventProducer {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public WorkflowEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes a "project.created" message.
     * Idempotency key = projectId — consumers must de-duplicate on this field.
     */
    @Async
    @EventListener
    public void onProjectCreated(ProjectCreatedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType",   "PROJECT_CREATED");
        payload.put("projectId",   event.getProjectId());
        payload.put("tenantId",    event.getTenantId());
        payload.put("idempotencyKey", "project-" + event.getProjectId());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RK_PROJECT_CREATED,
                    payload);
            log.info("[PRODUCER] Published project.created: projectId={} tenantId={}",
                    event.getProjectId(), event.getTenantId());
        } catch (Exception e) {
            log.warn("[PRODUCER] Failed to publish project.created (broker unavailable?): {}", e.getMessage());
        }
    }

    /**
     * Publishes a "task.created" message.
     * Idempotency key = taskId.
     */
    @Async
    @EventListener
    public void onTaskCreated(TaskCreatedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType",   "TASK_CREATED");
        payload.put("taskId",      event.getTaskId());
        payload.put("projectId",   event.getProjectId());
        payload.put("tenantId",    event.getTenantId());
        payload.put("idempotencyKey", "task-" + event.getTaskId());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RK_TASK_CREATED,
                    payload);
            log.info("[PRODUCER] Published task.created: taskId={} projectId={} tenantId={}",
                    event.getTaskId(), event.getProjectId(), event.getTenantId());
        } catch (Exception e) {
            log.warn("[PRODUCER] Failed to publish task.created (broker unavailable?): {}", e.getMessage());
        }
    }
}
