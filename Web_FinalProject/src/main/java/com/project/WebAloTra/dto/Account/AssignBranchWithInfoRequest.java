package com.project.WebAloTra.dto.Account;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignBranchWithInfoRequest {
    private Long accountId;
    private String branchCode;
    private String branchName;
    private String branchAddress;
    private String branchPhone;
    private String branchEmail;
}
