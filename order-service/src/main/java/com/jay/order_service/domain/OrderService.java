package com.jay.order_service.domain;

import com.jay.order_service.domain.models.*;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final OrderValidator orderValidator;
    private final OrderEventService orderEventService;

    private static final List<String> DELIVERY_ALLOWED_COUNTRIES = List.of("INDIA", "USA");

    public OrderService(
            OrderRepository orderRepository, OrderValidator orderValidator, OrderEventService orderEventService) {
        this.orderRepository = orderRepository;
        this.orderValidator = orderValidator;
        this.orderEventService = orderEventService;
    }

    public OrderCreationResponse createOrder(String userName, OrderCreationRequest request) {
        orderValidator.validateOrder(request);
        OrderEntity orderEntity = OrderMapper.convertToEntity(request);
        orderEntity.setUserName(userName);
        OrderEntity savedOrderEntity = orderRepository.save(orderEntity);
        log.info("Created Order with orderNumber={}", savedOrderEntity.getOrderNumber());
        OrderCreatedEvent orderCreatedEvent = OrderEventMapper.buildOrderCreatedEvent(orderEntity);
        orderEventService.save(orderCreatedEvent);
        return new OrderCreationResponse(savedOrderEntity.getOrderNumber());
    }

    public void processNewOrders() {
        List<OrderEntity> orderEntityList = orderRepository.findByStatus(OrderStatus.NEW);
        for (OrderEntity orderEntity : orderEntityList) {
            this.processOrder(orderEntity);
        }
    }

    private void processOrder(OrderEntity orderEntity) {
        try {
            if (checkEligibilityForDelivery(orderEntity)) {
                log.info("OrderNumber: {} can be delivered", orderEntity.getOrderNumber());
                orderRepository.updateOrderStatus(orderEntity.getOrderNumber(), OrderStatus.DELIVERED);
                OrderDeliveredEvent orderDeliveredEvent = OrderEventMapper.buildOrderDeliveredEvent(orderEntity);
                orderEventService.save(orderDeliveredEvent);
            } else {
                log.info("OrderNumber: {} can't be delivered to the provided Location", orderEntity.getOrderNumber());
                orderRepository.updateOrderStatus(orderEntity.getOrderNumber(), OrderStatus.CANCELLED);
                OrderCancelledEvent orderCancelledEvent = OrderEventMapper.buildOrderCancelledEvent(
                        orderEntity, "Can't deliver to the provided Location");
                orderEventService.save(orderCancelledEvent);
            }

        } catch (RuntimeException e) {
            log.error("Failed to process Order with orderNumber: {}", orderEntity.getOrderNumber(), e);
            orderRepository.updateOrderStatus(orderEntity.getOrderNumber(), OrderStatus.ERROR);
            OrderErrorEvent orderErrorEvent = OrderEventMapper.buildOrderErrorEvent(orderEntity, e.getMessage());
            orderEventService.save(orderErrorEvent);
        }
    }

    private boolean checkEligibilityForDelivery(OrderEntity orderEntity) {
        return DELIVERY_ALLOWED_COUNTRIES.contains(
                orderEntity.getDeliveryAddress().country().toUpperCase());
    }

    public List<OrderSummary> findOrders(String userName) {
        return orderRepository.findByUserName(userName);
    }

    public Optional<OrderDTO> getOrderDetailsByOrderNumber(String userName, String orderNumber) {
        return orderRepository
                .findByUserNameAndOrderNumber(userName, orderNumber)
                .map(OrderMapper::convertToDTO);
    }
}
