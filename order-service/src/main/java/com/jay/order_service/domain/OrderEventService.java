package com.jay.order_service.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jay.order_service.domain.models.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderEventService {
    private static final Logger log = LoggerFactory.getLogger(OrderEventService.class);

    private final OrderEventRepository orderEventRepository;

    private final ObjectMapper objectMapper;

    private final OrderEventPublisher orderEventPublisher;

    public OrderEventService(
            OrderEventRepository orderEventRepository,
            ObjectMapper objectMapper,
            OrderEventPublisher orderEventPublisher) {
        this.orderEventRepository = orderEventRepository;
        this.objectMapper = objectMapper;
        this.orderEventPublisher = orderEventPublisher;
    }

    public void save(OrderCreatedEvent orderCreatedEvent) {
        OrderEventEntity orderEventEntity = new OrderEventEntity();
        orderEventEntity.setEventId(orderCreatedEvent.eventId());
        orderEventEntity.setEventType(OrderEventType.ORDER_CREATED);
        orderEventEntity.setOrderNumber(orderCreatedEvent.orderNumber());
        orderEventEntity.setPayload(convertToJSON(orderCreatedEvent));
        orderEventEntity.setCreatedAt(orderCreatedEvent.createdAt());
        orderEventRepository.save(orderEventEntity);
    }

    public void save(OrderDeliveredEvent orderDeliveredEvent) {
        OrderEventEntity orderEventEntity = new OrderEventEntity();
        orderEventEntity.setEventId(orderDeliveredEvent.eventId());
        orderEventEntity.setEventType(OrderEventType.ORDER_DELIVERED);
        orderEventEntity.setOrderNumber(orderDeliveredEvent.orderNumber());
        orderEventEntity.setPayload(convertToJSON(orderDeliveredEvent));
        orderEventEntity.setCreatedAt(orderDeliveredEvent.createdAt());
        orderEventRepository.save(orderEventEntity);
    }

    public void save(OrderCancelledEvent orderCancelledEvent) {
        OrderEventEntity orderEventEntity = new OrderEventEntity();
        orderEventEntity.setEventId(orderCancelledEvent.eventId());
        orderEventEntity.setEventType(OrderEventType.ORDER_CANCELLED);
        orderEventEntity.setOrderNumber(orderCancelledEvent.orderNumber());
        orderEventEntity.setPayload(convertToJSON(orderCancelledEvent));
        orderEventEntity.setCreatedAt(orderCancelledEvent.createdAt());
        orderEventRepository.save(orderEventEntity);
    }

    public void save(OrderErrorEvent orderErrorEvent) {
        OrderEventEntity orderEventEntity = new OrderEventEntity();
        orderEventEntity.setEventId(orderErrorEvent.eventId());
        orderEventEntity.setEventType(OrderEventType.ORDER_PROCESSING_FAILED);
        orderEventEntity.setOrderNumber(orderErrorEvent.orderNumber());
        orderEventEntity.setPayload(convertToJSON(orderErrorEvent));
        orderEventEntity.setCreatedAt(orderErrorEvent.createdAt());
        orderEventRepository.save(orderEventEntity);
    }

    private String convertToJSON(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void publishOrderEvents() {
        List<OrderEventEntity> orderEventEntityList =
                orderEventRepository.findAll(Sort.by("createdAt").ascending());
        log.info("Found {} orders which can be published", orderEventEntityList.size());
        for (OrderEventEntity orderEventEntity : orderEventEntityList) {
            this.publishEvent(orderEventEntity);
            orderEventRepository.delete(orderEventEntity);
        }
    }

    private void publishEvent(OrderEventEntity orderEventEntity) {
        OrderEventType orderEventType = orderEventEntity.getEventType();
        switch (orderEventEntity.getEventType()) {
            case ORDER_CREATED:
                OrderCreatedEvent orderCreatedEvent =
                        ConvertJsonToObject(orderEventEntity.getPayload(), OrderCreatedEvent.class);
                orderEventPublisher.publish(orderCreatedEvent);
                break;
            case ORDER_DELIVERED:
                OrderDeliveredEvent orderDeliveredEvent =
                        ConvertJsonToObject(orderEventEntity.getPayload(), OrderDeliveredEvent.class);
                orderEventPublisher.publish(orderDeliveredEvent);
                break;
            case ORDER_CANCELLED:
                OrderCancelledEvent orderCancelledEvent =
                        ConvertJsonToObject(orderEventEntity.getPayload(), OrderCancelledEvent.class);
                orderEventPublisher.publish(orderCancelledEvent);
                break;
            case ORDER_PROCESSING_FAILED:
                OrderErrorEvent orderErrorEvent =
                        ConvertJsonToObject(orderEventEntity.getPayload(), OrderErrorEvent.class);
                orderEventPublisher.publish(orderErrorEvent);
                break;
            default:
                log.warn("Unsupported OrderEventType: {}", orderEventEntity.getEventType());
        }
    }

    private <T> T ConvertJsonToObject(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
