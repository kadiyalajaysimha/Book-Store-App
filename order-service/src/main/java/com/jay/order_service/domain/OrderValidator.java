package com.jay.order_service.domain;

import com.jay.order_service.clients.catalog.Product;
import com.jay.order_service.clients.catalog.ProductServiceClient;
import com.jay.order_service.domain.models.InValidOrderException;
import com.jay.order_service.domain.models.OrderCreationRequest;
import com.jay.order_service.domain.models.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderValidator {

    private static final Logger log = LoggerFactory.getLogger(OrderValidator.class);
    private ProductServiceClient productServiceClient;

    public OrderValidator(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    public boolean validateOrder(OrderCreationRequest orderCreationRequest) {
        for (OrderItem orderItem : orderCreationRequest.items()) {
            Product product = productServiceClient
                    .getProductByCode(orderItem.code())
                    .orElseThrow(
                            () -> new InValidOrderException("Product with code: " + orderItem.code() + " not found"));
            if (orderItem.price().compareTo(product.price()) != 0) {
                String errorString = "Product price does not match Actual price: " + product.price()
                        + " Received price: {}" + orderItem.price();
                log.error(errorString);
                throw new InValidOrderException(errorString);
            }
        }
        return true;
    }
}
