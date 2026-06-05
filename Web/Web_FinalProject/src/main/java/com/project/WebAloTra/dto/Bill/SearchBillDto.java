package com.project.WebAloTra.dto.Bill;

import lombok.Data;

import java.time.LocalDateTime;

import com.project.WebAloTra.entity.enumClass.BillStatus;

@Data
public class SearchBillDto {
    private String keyword;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private BillStatus billStatus;
}
