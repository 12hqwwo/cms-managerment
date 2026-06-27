package com.project.WebAloTra.dto.AddressShipping;

import com.project.WebAloTra.entity.Customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressShippingDtoAdmin {
    private Long id;
    private String address;
    private Long customerId;
}
