package com.nguner.yeditepecardshop.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // Eğer kullanılmıyorsa kaldırılabilir

@Data
@AllArgsConstructor
@NoArgsConstructor // NoArgsConstructor önemli, özellikle MongoDB entity'leri için
@Builder
@ToString
@Document(collection = "carts")
public class Cart {

    @Id
    private String id;
    
    private String userId;

    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    private Double subtotal;      // Yeni alan
    private Double shippingCost;  // Yeni alan
    private Double cartTotal;     // Yeniden adlandırılmış ve standartlaştırılmış alan (eski TotalPrice)

    // Lombok @Data, @AllArgsConstructor ve @NoArgsConstructor bu ihtiyaçları karşılar.
    // Manuel constructor ve getter/setter'lar kaldırıldı.
}

