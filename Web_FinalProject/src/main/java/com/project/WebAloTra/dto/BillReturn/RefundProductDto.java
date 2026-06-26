package com.project.WebAloTra.dto.BillReturn;

import com.project.WebAloTra.dto.Product.ProductDetailDto;
import com.project.WebAloTra.dto.Product.ProductDto;

import lombok.Data;

@Data
public class RefundProductDto {
    private String productName;
    private Long productDetailId;
    private String size;
    private String color;

    //Giá trả lại
    private Double momentPriceRefund;

    //Số lượng khách hàng trả lại
    private Integer quantityRefund;
}
