package com.jay.catalog_service.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest(
        properties = {"spring.test.database.replace=none", "spring.datasource.url =jdbc:tc:postgresql:16-alpine:///db"})
@Sql("/test-data.sql")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findByCodeTest() {
        ProductEntity productEntity = productRepository.findByCode("P104").orElseThrow();
        assertThat(productEntity.getCode()).isEqualTo("P104");
        assertThat(productEntity.getName()).isEqualTo("The Fault in Our Stars");
        assertThat(productEntity.getDescription())
                .isEqualTo(
                        "Despite the tumor-shrinking medical miracle that has bought her a few years, Hazel has never been anything but terminal, her final chapter inscribed upon diagnosis.");
        assertThat(productEntity.getPrice()).isEqualTo(new BigDecimal("14.50"));
        //        assertThrows(ProductNotFoundException.class,() ->
        // productRepository.findByCode("P10011").orElseThrow(()->new ProductNotFoundException("Product not Found")));
    }

    @Test
    void findByCodeTestWithInvalidCode() {
        //        assertThrows(ProductNotFoundException.class,() ->
        // productRepository.findByCode("P10011").orElseThrow(()->new ProductNotFoundException("Product not Found")));
        assertThat(productRepository.findByCode("P10011")).isEmpty();
    }
}
