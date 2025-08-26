package com.jay.order_service.web.controllers;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.jay.order_service.AbstractIT;
import com.jay.order_service.domain.models.OrderSummary;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

@Sql("/test-orders.sql")
public class OrderControllerTest extends AbstractIT {

    @Nested
    class createOrderTests {
        @Test
        void CreateOrderSuccessTest() {
            mockGetProductByCode("P100", new BigDecimal(34));
            var payload =
                    """
                    {
                      "items": [
                        {
                              "code": "P100",
                              "name": "Wireless Mouse",
                              "price": 34.0,
                              "quantity": 8
                            }
                      ],
                      "customer": {
                        "name": "Alice Johnson",
                        "email": "alice.johnson@example.com",
                        "phone": "+1-555-123-4567"
                      },
                      "deliveryAddress": {
                        "addressLine1": "123 Main Street",
                        "addressLine2": "Apt 4B",
                        "city": "New York",
                        "state": "NY",
                        "zipCode": "10001",
                        "country": "USA"
                      }
                    }
                    """;
            given().contentType(ContentType.JSON)
                    .body(payload)
                    .when()
                    .post("/api/orders/createOrder")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("orderNumber", notNullValue());
        }

        @Test
        void badRequestTest() {
            var payload =
                    """
                    {
                      "items": [
                        {
                          "code": "ITEM-1001",
                          "name": "Wireless Mouse",
                          "price": 25.99,
                          "quantity": 2
                        }
                      ],
                      "customer": {

                        "email": "alice.johnson@example.com",
                        "phone": "+1-555-123-4567"
                      },
                      "deliveryAddress": {

                        "addressLine2": "Apt 4B",
                        "city": "New York",
                        "state": "NY",
                        "zipCode": "10001",
                        "country": "USA"
                      }
                    }
                    """;
            given().contentType(ContentType.JSON)
                    .body(payload)
                    .when()
                    .post("/api/orders/createOrder")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class GetOrdersTests {
        @Test
        void getAllOrdersTest() {
            List<OrderSummary> ordersList = given().when()
                    .get("/api/orders/getAllOrders")
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .as(new TypeRef<>() {});
            assertThat(ordersList).hasSize(2);
        }
    }

    @Nested
    class GetOrderDetailsByOrderNumberTests {

        @Test
        void getOrderDetailsByOrderNumberTest() {
            String orderNumber = "order-456";
            given().when()
                    .get("/api/orders/getOrder/{orderNumber}", orderNumber)
                    .then()
                    .statusCode(200)
                    .body("orderNumber", is("order-456"))
                    .body("items.size()", is(1));
        }
    }
}
