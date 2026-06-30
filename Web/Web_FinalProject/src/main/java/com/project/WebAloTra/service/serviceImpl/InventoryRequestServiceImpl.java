package com.project.WebAloTra.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.WebAloTra.dto.InventoryRequest.InventoryRequestDetailDto;
import com.project.WebAloTra.dto.InventoryRequest.InventoryRequestDto;
import com.project.WebAloTra.entity.*;
import com.project.WebAloTra.entity.enumClass.RequestStatus;
import com.project.WebAloTra.repository.*;
import com.project.WebAloTra.service.InventoryRequestService;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryRequestServiceImpl implements InventoryRequestService {

    @Autowired
    private InventoryRequestRepository requestRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProductDetailRepository productDetailRepository;

    @Override
    @Transactional
    public InventoryRequest createRequest(InventoryRequestDto requestDto, String userEmail) {
        Account account = accountRepository.findByEmail(userEmail);
        if (account == null) throw new RuntimeException("Account not found");

        Branch branch = branchRepository.findById(requestDto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        InventoryRequest request = new InventoryRequest();
        request.setBranch(branch);
        request.setCreatedBy(account);
        request.setNote(requestDto.getNote());
        request.setStatus(RequestStatus.PENDING);

        List<InventoryRequestDetail> details = new ArrayList<>();
        for (InventoryRequestDetailDto detailDto : requestDto.getRequestDetails()) {
            ProductDetail productDetail = productDetailRepository.findById(detailDto.getProductDetailId())
                    .orElseThrow(() -> new RuntimeException("ProductDetail not found"));
            
            InventoryRequestDetail detail = new InventoryRequestDetail();
            detail.setInventoryRequest(request);
            detail.setProductDetail(productDetail);
            detail.setRequestedQuantity(detailDto.getRequestedQuantity());
            detail.setApprovedQuantity(0);
            
            details.add(detail);
        }
        
        request.setRequestDetails(details);
        return requestRepository.save(request);
    }

    @Override
    public List<InventoryRequest> getRequestsByBranch(Long branchId) {
        return requestRepository.findByBranchIdOrderByCreatedAtDesc(branchId);
    }
}
