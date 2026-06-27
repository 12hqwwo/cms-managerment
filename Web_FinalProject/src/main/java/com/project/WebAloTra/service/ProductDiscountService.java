package com.project.WebAloTra.service;

import java.util.List;

import com.project.WebAloTra.dto.ProductDiscount.ProductDiscountCreateDto;
import com.project.WebAloTra.dto.ProductDiscount.ProductDiscountDto;
import com.project.WebAloTra.entity.ProductDiscount;

public interface ProductDiscountService {
    List<ProductDiscount> getAllProductDiscount();

    ProductDiscountDto updateCloseProductDiscount(Long discountId, boolean closed);

    List<ProductDiscountDto> createProductDiscountMultiple(ProductDiscountCreateDto productDiscountCreateDto);
}
