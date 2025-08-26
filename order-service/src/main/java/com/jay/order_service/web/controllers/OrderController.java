package com.jay.order_service.web.controllers;

import com.jay.order_service.domain.OrderNotFoundException;
import com.jay.order_service.domain.OrderService;
import com.jay.order_service.domain.SecurityService;
import com.jay.order_service.domain.models.OrderCreationRequest;
import com.jay.order_service.domain.models.OrderCreationResponse;
import com.jay.order_service.domain.models.OrderDTO;
import com.jay.order_service.domain.models.OrderSummary;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
class OrderController {
    private final SecurityService securityService;
    private final OrderService orderService;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    OrderController(SecurityService securityService, OrderService orderService) {
        this.securityService = securityService;
        this.orderService = orderService;
    }

    @PostMapping("/createOrder")
    @ResponseStatus(HttpStatus.CREATED)
    OrderCreationResponse createOrder(@Valid @RequestBody OrderCreationRequest request) {
        String userName = securityService.getLoginUserName();
        log.info("Creating order for user: {}", userName);
        return orderService.createOrder(userName, request);
    }

    @GetMapping("/getAllOrders")
    List<OrderSummary> getOrders() {
        String userName = securityService.getLoginUserName();
        log.info("Fetching orders for user: {}", userName);
        return orderService.findOrders(userName);
    }

    @GetMapping("/getOrder/{orderNumber}")
    public OrderDTO getOrder(@PathVariable("orderNumber") String orderNumber) {
        log.info("Fetching order by id: {}", orderNumber);
        String userName = securityService.getLoginUserName();
        return orderService
                .getOrderDetailsByOrderNumber(userName, orderNumber)
                .orElseThrow(
                        () -> new OrderNotFoundException("Could not find the order: " + orderNumber + " of the User"));
    }
}
