package com.project.WebAloTra.dto.InventoryRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequestDto {
    private String note;
    private Long branchId;
    private List<InventoryRequestDetailDto> requestDetails;
}
