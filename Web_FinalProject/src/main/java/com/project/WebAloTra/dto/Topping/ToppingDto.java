// Đường dẫn: WebsocketAndWishlist/src/main/java/com/project/DuAnTotNghiep/dto/ToppingDTO.java

package com.project.WebAloTra.dto.Topping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToppingDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private Boolean status;
}