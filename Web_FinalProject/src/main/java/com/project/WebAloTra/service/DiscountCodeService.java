package com.project.WebAloTra.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.project.WebAloTra.dto.DiscountCode.DiscountCodeDto;
import com.project.WebAloTra.dto.DiscountCode.SearchDiscountCodeDto;
import com.project.WebAloTra.dto.Product.SearchProductDto;
import com.project.WebAloTra.entity.DiscountCode;

public interface DiscountCodeService {
    Page<DiscountCodeDto> getAllDiscountCode(SearchDiscountCodeDto searchDiscountCodeDto, Pageable pageable);
    DiscountCodeDto saveDiscountCode(DiscountCodeDto discountCodeDto);
    DiscountCodeDto updateDiscountCode(DiscountCodeDto discountCodeDto);

    DiscountCodeDto getDiscountCodeById(Long id);
    DiscountCodeDto getDiscountCodeByCode(Long code);
    DiscountCodeDto updateStatus(Long discountCodeId, int status);
    Page<DiscountCodeDto> getAllAvailableDiscountCode(Pageable pageable);
}
