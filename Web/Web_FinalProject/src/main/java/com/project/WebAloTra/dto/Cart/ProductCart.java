package com.project.WebAloTra.dto.Cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.project.WebAloTra.entity.Color;
import com.project.WebAloTra.entity.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCart {
    private Long productId;
    private String code;
    private String name;
    private Long materialId;
    private LocalDateTime createDate;
    private LocalDateTime updatedDate;
    private String describe;
    private Long brandId;
    private Long categoryId;
    private String imageUrl;
    private Double price;


}
