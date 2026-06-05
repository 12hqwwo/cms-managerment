package com.project.WebAloTra.dto.Cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

import com.project.WebAloTra.dto.Product.ProductDetailDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDto {
    private Long id;
    private Long accountId;
    private ProductCart product;
    private ProductDetailDto detail;

    @NotNull
    private int quantity;
    private LocalDateTime createDate;
    private LocalDateTime updatedDate;

}

