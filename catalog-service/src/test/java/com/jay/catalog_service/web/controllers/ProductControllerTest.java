package com.jay.catalog_service.web.controllers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.jay.catalog_service.AbstractIT;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

@Sql("/test-data.sql")
class ProductControllerTest extends AbstractIT {

    @Test
    void getProductsTest() {
        given().contentType(ContentType.JSON)
                .when()
                .get("/api/products/getProducts")
                .then()
                .statusCode(200)
                .body("data", hasSize(10))
                .body("totalElements", is(15))
                .body("pageNumber", is(1))
                .body("totalPages", is(2))
                .body("isFirst", is(true))
                .body("isLast", is(false))
                .body("hasNext", is(true))
                .body("hasPrevious", is(false));
    }

    @Test
    void getProductByCodeTest() {
        given().contentType(ContentType.JSON)
                .when()
                .get("/api/products/getProductsByCode/{code}", "P104")
                .then()
                .statusCode(200)
                .body("code", is("P104"))
                .body("name", is("The Fault in Our Stars"))
                .body("imageUrl", is("https://images.gr-assets.com/books/1360206420l/11870085.jpg"));
    }

    @Test
    void shouldReturnNotFoundWhenProductCodeNotExists() {
        given().contentType(ContentType.JSON)
                .when()
                .get("/api/products/getProductsByCode/{code}", "P100000000")
                .then()
                .statusCode(404)
                .body("status", is(404))
                .body("title", is("Product Not Found"));
    }
}
