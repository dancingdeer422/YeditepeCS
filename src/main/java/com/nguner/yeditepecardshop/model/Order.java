package com.nguner.yeditepecardshop.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    private String userId;
    private List<OrderItem> items;
    private Address shippingAddress;
    private LocalDateTime orderDate;
    private String status;

}
