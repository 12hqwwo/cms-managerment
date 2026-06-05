package com.project.WebAloTra.dto.Product;

import lombok.Data;

import java.util.List;

import com.project.WebAloTra.entity.ProductDetail;

@Data
public class CreateProductDetailsForm {
    private List<ProductDetail> productDetailList;
}
