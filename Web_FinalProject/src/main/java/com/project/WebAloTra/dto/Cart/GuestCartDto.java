package com.project.WebAloTra.dto.Cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestCartDto {
    private Long productId;
    private String name;
    private String imageUrl;
    private Double price;
    private int quantity;
}
