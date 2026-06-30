package com.project.WebAloTra.dto.Account;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateVendorOnlyRequest {
    private String email;
    private String password;
    private String code;
    private String name;
    private String phoneNumber;
    
    // Tuỳ chọn: ID của chi nhánh nếu muốn gán ngay
    private Long branchId;
}
