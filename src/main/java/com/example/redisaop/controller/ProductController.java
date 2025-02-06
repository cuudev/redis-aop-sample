package com.example.redisaop.controller;

import com.example.redisaop.annotation.RedisCacheEvict;
import com.example.redisaop.annotation.RedisCacheable;
import com.example.redisaop.dto.ProductPayload;
import com.example.redisaop.entity.Product;
import com.example.redisaop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping()
    @RedisCacheable(prefix = "PRODUCT")
    public List<Product> find( @RequestParam(value="foo", required=false, defaultValue = "0") Integer foo,  @RequestParam(value="bar", required=false, defaultValue = "0") Integer bar) {
        return this.productService.findAll();
    }

    @GetMapping("{id}")
    @RedisCacheable(prefix = "PRODUCT")
    public Optional<Product> findById(@PathVariable("id") Long id) {
        return this.productService.findById(id);
    }
    @DeleteMapping("{id}")
    @RedisCacheEvict(prefix = "PRODUCT")
    public Optional<Product> deleteCache(@PathVariable("id") Long id) {
        return this.productService.findById(id);
    }

    @PostMapping()
    public Product save(@RequestBody ProductPayload productPayload) {
        return this.productService.save(productPayload);
    }

    @PutMapping()
    @RedisCacheEvict(prefix = "PRODUCT")
    public Product update(@RequestBody ProductPayload productPayload) {
        return this.productService.update(productPayload);
    }
}
