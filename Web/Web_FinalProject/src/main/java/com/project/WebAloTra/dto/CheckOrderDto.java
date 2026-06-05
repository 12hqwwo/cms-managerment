package com.project.WebAloTra.dto;

import com.project.WebAloTra.dto.Product.ProductDetailDto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckOrderDto {
    private Long productDetailId;
    private String quantity;
}
