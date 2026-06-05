package com.project.WebAloTra.dto.Order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//import thêm:
import java.util.List;

import com.project.WebAloTra.dto.Order.ToppingOrderDto;
import com.project.WebAloTra.dto.Product.ProductDetailDto;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {
    private Long productDetailId;
    private Integer quantity;
    private List<ToppingOrderDto> toppings; // ✅ thêm dòng này
}
