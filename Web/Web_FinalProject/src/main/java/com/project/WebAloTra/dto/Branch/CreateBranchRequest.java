package com.project.WebAloTra.dto.Branch;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateBranchRequest {
    private String branchCode;
    private String branchName;
    private String address;
    private String phone;
    private String email;
}
