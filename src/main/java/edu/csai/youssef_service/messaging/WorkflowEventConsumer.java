package edu.csai.youssef_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consumer — listens to the workhub.tasks queue.
 *
 * Implements a basic reliability strategy: idempotency.
 * In a real system, this would be backed by a database table or Redis cache.
 * Here, we use an in-memory set to deduplicate processed messages.
 */
@Component
public class WorkflowEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEventConsumer.class);

    // In-memory idempotency cache (e.g., Redis or DB table in production)
    private final Set<String> processedKeys = ConcurrentHashMap.newKeySet();

    @RabbitListener(queues = RabbitMQConfig.QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void receiveTaskMessage(Map<String, Object> payload) {
        String eventType = (String) payload.get("eventType");
        String idempotencyKey = (String) payload.get("idempotencyKey");

        // 1. Check idempotency cache
        if (idempotencyKey != null && !processedKeys.add(idempotencyKey)) {
            log.info("[CONSUMER] Duplicate message ignored: idempotencyKey={}", idempotencyKey);
            return; // Already processed
        }

        try {
            // 2. Process message based on type
            if ("PROJECT_CREATED".equals(eventType)) {
                log.info("[CONSUMER] Processing PROJECT_CREATED: projectId={} tenantId={}",
                        payload.get("projectId"), payload.get("tenantId"));
                // Simulate some work (e.g., sending an email, initializing default tasks)
                Thread.sleep(500); 
            } else if ("TASK_CREATED".equals(eventType)) {
                log.info("[CONSUMER] Processing TASK_CREATED: taskId={} projectId={} tenantId={}",
                        payload.get("taskId"), payload.get("projectId"), payload.get("tenantId"));
                // Simulate work
                Thread.sleep(200);
            } else {
                log.warn("[CONSUMER] Unknown eventType: {}", eventType);
            }
        } catch (Exception e) {
            log.error("[CONSUMER] Error processing message (idempotencyKey={}): {}", idempotencyKey, e.getMessage());
            // If we throw here, RabbitMQ will retry (up to max attempts), 
            // then send to DLQ. We must remove the key from cache so retries can process it.
            if (idempotencyKey != null) {
                processedKeys.remove(idempotencyKey);
            }
            throw new RuntimeException("Message processing failed", e);
        }
    }
}
