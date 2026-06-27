package com.project.WebAloTra.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.project.WebAloTra.dto.CustomerDto.CustomerDto;


public interface CustomerService {
    Page<CustomerDto> getAllCustomers(Pageable pageable);

    CustomerDto createCustomerAdmin(CustomerDto customerDto);

    Page<CustomerDto> searchCustomerAdmin(String keyword, Pageable pageable);
}
