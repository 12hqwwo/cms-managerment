package com.project.WebAloTra.dto.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.project.WebAloTra.entity.Image;
import com.project.WebAloTra.entity.Material;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String categoryName;
    private String imageUrl;
    private Double priceMin;

    private List<ProductDetailDto> productDetailDtos;
    private LocalDateTime createDate;
    private LocalDateTime updatedDate;
    private boolean isDiscounted;
}
