package com.jay.order_service.jobs;

import com.jay.order_service.domain.OrderService;
import java.time.Instant;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessingJob {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsPublishingJob.class);
    private final OrderService orderService;

    public OrderProcessingJob(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(cron = "${orders.processNewOrdersCronJob}")
    @SchedulerLock(name = "publishNewOrderEvents")
    public void publishNewOrderEvents() {
        LockAssert.assertLocked();
        log.info("Processing new Orders at {}", Instant.now());
        orderService.processNewOrders();
    }
}
