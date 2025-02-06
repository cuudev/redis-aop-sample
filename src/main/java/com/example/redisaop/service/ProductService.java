package com.example.redisaop.service;

import com.example.redisaop.annotation.RedisCacheEvict;
import com.example.redisaop.annotation.RedisCacheable;
import com.example.redisaop.dto.ProductPayload;
import com.example.redisaop.entity.Product;
import com.example.redisaop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // ProductService.findAll
    @RedisCacheable(prefix = "PRODUCT")
    public List<Product> findAll() {
        String sql = FormatStyle.BASIC.getFormatter().format("");
        return this.productRepository.findAll();
    }

    @RedisCacheable(prefix = "PRODUCT")
    // ProductService.findAll-100000
    public Optional<Product> findById(Long id) {
        return this.productRepository.findById(id);
    }

    public Product save(ProductPayload productPayload) {
        Product product = Product.toProduct(productPayload);
        return productRepository.save(product);
    }

    @RedisCacheEvict(prefix = "PRODUCT", clearAll = true)
    public Product update(ProductPayload productPayload) {
        Optional<Product> product = productRepository.findById(productPayload.getId());
        return product.map(productRepository::save).orElse(null);
    }
}
