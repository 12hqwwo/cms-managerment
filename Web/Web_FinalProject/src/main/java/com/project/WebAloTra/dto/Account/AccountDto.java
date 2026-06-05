package com.project.WebAloTra.dto.Account;

import lombok.Data;

import java.util.List;

import com.project.WebAloTra.dto.AddressShipping.AddressShippingDto;

@Data
public class AccountDto {
    private String phoneNumber;
    private String name;
    private String email;
    private String password;
    private List<AddressShippingDto> addressShippingList;
}
