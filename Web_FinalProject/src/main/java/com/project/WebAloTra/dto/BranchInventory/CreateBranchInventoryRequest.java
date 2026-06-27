package com.project.WebAloTra.dto.BranchInventory;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateBranchInventoryRequest {
    private Long branchId;
    private Long productDetailId;
    private Integer quantity;
    private Integer minQuantity;
    private Integer maxQuantity;
}
