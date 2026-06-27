package com.project.WebAloTra.dto.Account;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateVendorAccountRequest {
    private String email;
    private String password;
    private String code;
    private String phoneNumber;
    // Branch info
    private String branchCode;
    private String branchName;
    private String branchAddress;
    private String branchPhone;
    private String branchEmail;
}
