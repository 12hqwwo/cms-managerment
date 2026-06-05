package com.project.WebAloTra.dto.Account;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignBranchRequest {
    private Long accountId;
    private Long branchId;
}
