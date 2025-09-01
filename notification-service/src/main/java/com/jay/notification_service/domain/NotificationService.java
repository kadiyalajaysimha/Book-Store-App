package com.jay.notification_service.domain;

import com.jay.notification_service.ApplicationProperties;
import com.jay.notification_service.domain.models.OrderCancelledEvent;
import com.jay.notification_service.domain.models.OrderCreatedEvent;
import com.jay.notification_service.domain.models.OrderDeliveredEvent;
import com.jay.notification_service.domain.models.OrderErrorEvent;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender emailSender;
    private final ApplicationProperties applicationProperties;

    public NotificationService(JavaMailSender emailSender, ApplicationProperties applicationProperties) {
        this.emailSender = emailSender;
        this.applicationProperties = applicationProperties;
    }

    public void sendOrderCreatedNotification(OrderCreatedEvent orderCreatedEvent) {
        String message =
                """
                Hello %s,

                Order: %s has been placed.

                Thanks,
                Bookstore
                """
                        .formatted(orderCreatedEvent.customer().name(), orderCreatedEvent.orderNumber());
        log.info("\n{}", message);
        sendEmail(orderCreatedEvent.customer().email(), "Order Created Notification", message);
    }

    public void sendOrderCancelledNotification(OrderCancelledEvent orderCancelledEvent) {
        String message =
                """
                Hello %s,

                Order: %s has been cancelled. At the moment, we can't deliver to the provided address.

                Thanks,
                Bookstore
                """
                        .formatted(orderCancelledEvent.customer().name(), orderCancelledEvent.orderNumber());
        log.info("\n{}", message);
        sendEmail(orderCancelledEvent.customer().email(), "Order Cancelled Notification", message);
    }

    public void sendOrderDeliveredNotification(OrderDeliveredEvent orderDeliveredEvent) {
        String message =
                """
                Hello %s,

                Order: %s has been delivered.

                Thanks,
                Bookstore
                """
                        .formatted(orderDeliveredEvent.customer().name(), orderDeliveredEvent.orderNumber());
        log.info("\n{}", message);
        sendEmail(orderDeliveredEvent.customer().email(), "Order Delivered Notification", message);
    }

    public void sendOrderErrorNotification(OrderErrorEvent orderErrorEvent) {
        String message =
                """
                Hello %s,

                Processing of Order: %s has failed.

                Thanks,
                Bookstore
                """
                        .formatted(orderErrorEvent.customer().name(), orderErrorEvent.orderNumber());
        log.info("\n{}", message);
        sendEmail(applicationProperties.supportEmail(), "Order Error Notification", message);
    }

    private void sendEmail(String recipient, String subject, String content) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(applicationProperties.supportEmail());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content);
            emailSender.send(mimeMessage);
            log.info("Email sent to: {}", recipient);
        } catch (Exception e) {
            throw new RuntimeException("Error while sending email", e);
        }
    }
}
