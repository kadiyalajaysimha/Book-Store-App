package com.jay.notification_service.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<OrderEventEntity, Long> {
    public Boolean existsByEventId(String eventId);
}
