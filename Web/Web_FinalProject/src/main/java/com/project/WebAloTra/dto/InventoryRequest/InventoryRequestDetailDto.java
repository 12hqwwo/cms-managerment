package com.project.WebAloTra.dto.InventoryRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequestDetailDto {
    private Long productDetailId;
    private Integer requestedQuantity;
}
