package com.project.WebAloTra.dto.Account;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VendorAccountResponse {
    private Long accountId;
    private String email;
    private String code;
    private Long branchId;
    private String branchCode;
    private String branchName;
    private String branchAddress;
    private String branchPhone;
    private String branchEmail;
    private String roleName;
    private boolean isActive;
}
