package com.project.WebAloTra.service;

import org.springframework.stereotype.Service;

import com.project.WebAloTra.dto.AddressShipping.AddressShippingDto;
import com.project.WebAloTra.dto.AddressShipping.AddressShippingDtoAdmin;
import com.project.WebAloTra.entity.AddressShipping;

import java.util.List;

@Service
public interface AddressShippingService {
    List<AddressShippingDto> getAddressShippingByAccountId();
    AddressShippingDto saveAddressShippingUser(AddressShippingDto addressShippingDto);

    AddressShippingDto saveAddressShippingAdmin(AddressShippingDtoAdmin addressShippingDto);

    void deleteAddressShipping(Long id);
}
