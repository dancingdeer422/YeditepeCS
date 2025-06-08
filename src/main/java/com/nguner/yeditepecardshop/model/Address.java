package com.nguner.yeditepecardshop.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@Document
public class Address {
    @Id
    private String id;
    private String title;
    private String addressLine;
    private String city;
    private String district;
    private String zipCode;
    
    // Keep old field for backward compatibility
    private String street;
    
    // Constructor for easy creation
    public Address(String title, String addressLine, String city, String district, String zipCode) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.addressLine = addressLine;
        this.city = city;
        this.district = district;
        this.zipCode = zipCode;
        this.street = addressLine; // for backward compatibility
    }
} 