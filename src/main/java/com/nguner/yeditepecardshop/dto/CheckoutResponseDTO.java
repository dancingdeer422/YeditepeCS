package com.nguner.yeditepecardshop.dto;

import com.nguner.yeditepecardshop.model.CartItem;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CheckoutResponseDTO {
    //private Invoice invoice;
    private List<CartItem> purchasedItems;
    private Double totalAmount;
}
