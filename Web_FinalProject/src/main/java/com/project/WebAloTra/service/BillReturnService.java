package com.project.WebAloTra.service;


import java.util.List;

import com.project.WebAloTra.dto.BillReturn.BillReturnCreateDto;
import com.project.WebAloTra.dto.BillReturn.BillReturnDetailDto;
import com.project.WebAloTra.dto.BillReturn.BillReturnDto;
import com.project.WebAloTra.dto.BillReturn.SearchBillReturnDto;

public interface BillReturnService {
    List<BillReturnDto> getAllBillReturns(SearchBillReturnDto searchBillReturnDto);

    BillReturnDto createBillReturn(BillReturnCreateDto billReturnCreateDto);

    BillReturnDetailDto getBillReturnDetailById(Long id);
    BillReturnDetailDto getBillReturnDetailByCode(String code);

    String generateHtmlContent(Long billReturnId);

    BillReturnDto updateStatus(Long id, int returnStatus);
}
