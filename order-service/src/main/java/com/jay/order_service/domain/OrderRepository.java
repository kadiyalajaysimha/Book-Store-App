package com.jay.order_service.domain;

import com.jay.order_service.domain.models.OrderStatus;
import com.jay.order_service.domain.models.OrderSummary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByStatus(OrderStatus status);

    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    default void updateOrderStatus(String orderNo, OrderStatus status) {
        OrderEntity order = this.findByOrderNumber(orderNo).orElseThrow();
        order.setStatus(status);
        this.save(order);
    }

    @Query(
            """
            Select new com.jay.order_service.domain.models.OrderSummary(o.orderNumber, o.status)
            from OrderEntity o where o.userName = :userName
            """)
    List<OrderSummary> findByUserName(String userName);

    @Query(
            """
           Select distinct o
            from OrderEntity o left join fetch o.items where o.userName = :userName and o.orderNumber = :orderNumber
            """)
    Optional<OrderEntity> findByUserNameAndOrderNumber(String userName, String orderNumber);
}
