package com.project.WebAloTra.service;

import javassist.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.project.WebAloTra.dto.Product.ProductDetailDto;
import com.project.WebAloTra.entity.Product;
import com.project.WebAloTra.entity.ProductDetail;

import java.util.List;

@Service
public interface ProductDetailService {
    ProductDetail save(ProductDetail productDetail);

    ProductDetail getProductDetailByProductCode(String code) throws NotFoundException;

    List<ProductDetailDto> getByProductId(Long id) throws com.project.WebAloTra.exception.NotFoundException;
}
