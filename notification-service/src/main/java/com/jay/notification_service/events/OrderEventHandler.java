package com.jay.notification_service.events;

import com.jay.notification_service.domain.NotificationRepository;
import com.jay.notification_service.domain.NotificationService;
import com.jay.notification_service.domain.OrderEventEntity;
import com.jay.notification_service.domain.models.OrderCancelledEvent;
import com.jay.notification_service.domain.models.OrderCreatedEvent;
import com.jay.notification_service.domain.models.OrderDeliveredEvent;
import com.jay.notification_service.domain.models.OrderErrorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public OrderEventHandler(NotificationRepository notificationRepository, NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${notifications.new-orders-queue}")
    void handleOrderEvent(OrderCreatedEvent orderCreatedEvent) {

        if (notificationRepository.existsByEventId(orderCreatedEvent.eventId())) {
            log.info("Received duplicate Order Created Event: {}", orderCreatedEvent.eventId());
            return;
        }
        log.info("Recieved Order Created Event: {} ", orderCreatedEvent.eventId());
        notificationService.sendOrderCreatedNotification(orderCreatedEvent);
        OrderEventEntity orderEventEntity = new OrderEventEntity(orderCreatedEvent.eventId());
        notificationRepository.save(orderEventEntity);
    }

    @RabbitListener(queues = "${notifications.cancelled-orders-queue}")
    void handleOrderEvent(OrderCancelledEvent orderCancelledEvent) {

        if (notificationRepository.existsByEventId(orderCancelledEvent.eventId())) {
            log.info("Received duplicate Order Cancelled Event: {}", orderCancelledEvent.eventId());
            return;
        }
        log.info("Recieved Order Cancelled Event: {} ", orderCancelledEvent.eventId());
        notificationService.sendOrderCancelledNotification(orderCancelledEvent);
        OrderEventEntity orderEventEntity = new OrderEventEntity(orderCancelledEvent.eventId());
        notificationRepository.save(orderEventEntity);
    }

    @RabbitListener(queues = "${notifications.delivered-orders-queue}")
    void handleOrderEvent(OrderDeliveredEvent orderDeliveredEvent) {

        if (notificationRepository.existsByEventId(orderDeliveredEvent.eventId())) {
            log.info("Received duplicate Order Delivered Event: {}", orderDeliveredEvent.eventId());
            return;
        }
        log.info("Recieved Order Delivered Event: {} ", orderDeliveredEvent.eventId());
        notificationService.sendOrderDeliveredNotification(orderDeliveredEvent);
        OrderEventEntity orderEventEntity = new OrderEventEntity(orderDeliveredEvent.eventId());
        notificationRepository.save(orderEventEntity);
    }

    @RabbitListener(queues = "${notifications.error-orders-queue}")
    void handleOrderEvent(OrderErrorEvent orderErrorEvent) {

        if (notificationRepository.existsByEventId(orderErrorEvent.eventId())) {
            log.info("Received duplicate Order Error Event: {}", orderErrorEvent.eventId());
            return;
        }
        log.info("Recieved Order Error Event: {} ", orderErrorEvent.eventId());
        notificationService.sendOrderErrorNotification(orderErrorEvent);
        OrderEventEntity orderEventEntity = new OrderEventEntity(orderErrorEvent.eventId());
        notificationRepository.save(orderEventEntity);
    }
}
