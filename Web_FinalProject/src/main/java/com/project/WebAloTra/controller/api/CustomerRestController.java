package com.project.WebAloTra.controller.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.dto.CustomerDto.CustomerDto;
import com.project.WebAloTra.service.CustomerService;

@RestController
public class CustomerRestController {

    private final CustomerService customerService;

    public CustomerRestController(CustomerService customerService) {
        this.customerService = customerService;
    }


    @GetMapping("/api/customer")
    public Page<CustomerDto> getAllCustomers(Pageable pageable) {
        return customerService.getAllCustomers(pageable);
    }

    @GetMapping("/api/customer/filter")
    public Page<CustomerDto> searchCustomers(@RequestParam String keyword,  Pageable pageable) {
        return customerService.searchCustomerAdmin(keyword, pageable);
    }

    @PostMapping("/customer/create")
    public CustomerDto createCustomerFormData(@ModelAttribute CustomerDto customerDto) {
        return customerService.createCustomerAdmin(customerDto);
    }

    @PostMapping("/api/customer")
    public CustomerDto createCustomer(@RequestBody CustomerDto customerDto) {
        return customerService.createCustomerAdmin(customerDto);
    }


}
