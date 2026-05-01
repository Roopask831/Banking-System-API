package com.banking.system.service;

import com.banking.system.entity.AuditLog;
import com.banking.system.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String username, String action, String resourceType,
                    String resourceId, String details, boolean success) {
        try {
            AuditLog entry = AuditLog.builder()
                    .username(username).action(action)
                    .resourceType(resourceType).resourceId(resourceId)
                    .details(details).success(success).build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage());
        }
    }

    @Async
    public void logSuccess(String username, String action, String resourceType, String resourceId) {
        log(username, action, resourceType, resourceId, null, true);
    }

    @Async
    public void logFailure(String username, String action, String details) {
        log(username, action, null, null, details, false);
    }
}