package com.nguner.yeditepecardshop.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Getter
@Setter
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String title;

    private String categoryName;

    private String model;

    private int stockQuantity;

    @Min(value = 0, message = "Base price cannot be lower than 0")
    private double basePrice;

    // New field for storing image as binary data
    private String imagePath;

    private LocalDateTime createdDate;

    // Additional fields for product details
    private String description;
    private double weight;
    private double length;
    private double width;
    private double height;
    private int cardsPerPack;
    private int packsPerBox;


    // Backward compatibility for existing code that uses quantityInStock
    public int getQuantityInStock() {
        return this.stockQuantity;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.stockQuantity = quantityInStock;
    }
}
