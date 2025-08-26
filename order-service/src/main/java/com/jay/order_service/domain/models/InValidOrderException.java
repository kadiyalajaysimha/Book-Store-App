package com.jay.order_service.domain.models;

public class InValidOrderException extends RuntimeException {
    public InValidOrderException(String message) {
        super(message);
    }
}
