package com.project.WebAloTra.service;

import com.project.WebAloTra.dto.InventoryRequest.InventoryRequestDto;
import com.project.WebAloTra.entity.InventoryRequest;

import java.util.List;

public interface InventoryRequestService {
    InventoryRequest createRequest(InventoryRequestDto requestDto, String userEmail);
    List<InventoryRequest> getRequestsByBranch(Long branchId);
}
