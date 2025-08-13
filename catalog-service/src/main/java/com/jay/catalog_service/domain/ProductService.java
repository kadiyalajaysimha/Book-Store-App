package com.jay.catalog_service.domain;

import com.jay.catalog_service.ApplicationProperties;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ApplicationProperties properties;

    ProductService(ProductRepository productRepository, ApplicationProperties properties) {
        this.productRepository = productRepository;
        this.properties = properties;
    }

    public PagedResult<Product> getProducts(int pageNo) {
        pageNo = pageNo <= 1 ? 0 : pageNo - 1;
        Page<Product> page = productRepository
                .findAll(PageRequest.of(
                        pageNo, properties.pageSize(), Sort.by("name").ascending()))
                .map(ProductMapper::toProduct);
        PagedResult<Product> pagedResult = new PagedResult<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious());

        return pagedResult;
    }

    public Optional<Product> getProductByCode(String code) {
        return productRepository.findByCode(code).map(ProductMapper::toProduct);
    }
}
