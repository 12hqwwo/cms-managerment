package com.project.WebAloTra.dto.Bill;

import lombok.*;

import java.time.LocalDateTime;

import com.project.WebAloTra.dto.CustomerDto.CustomerDto;
import com.project.WebAloTra.entity.enumClass.BillStatus;

@Data
public class BillDto {
    private Long id;
    private String code;
    private double promotionPrice;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private BillStatus status;
    private Boolean returnStatus;
    private CustomerDto customer;
    private Double totalAmount;
}
