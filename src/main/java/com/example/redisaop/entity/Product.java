package com.example.redisaop.entity;

import com.example.redisaop.dto.ProductPayload;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
    private Long id;

    @Column
    private String name;

    @Column
    private Float price;

    @Column
    private Long stock;

    public Product() {
    }

//    public Product(ProductPayload productPayload) {
//        this.id = productPayload.getId();
//        this.name = productPayload.getName();
//        this.price = productPayload.getPrice();
//        this.stock = productPayload.getStock();
//    }

    public static Product toProduct(ProductPayload productPayload) {
        Product product = new Product();
        product.setId(productPayload.getId());
        product.setName(productPayload.getName());
        product.setPrice(productPayload.getPrice());
        product.setStock(productPayload.getStock());

        return product;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
}
