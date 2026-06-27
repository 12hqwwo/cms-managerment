package com.project.WebAloTra.dto.BillReturn;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.project.WebAloTra.dto.CustomerDto.CustomerDto;

import java.time.LocalDateTime;

@Data
public class BillReturnDto {

    private Long id;

    private String code;

    private String returnReason;

    private CustomerDto customer;

    private LocalDateTime returnDate;

    private Double returnMoney;

    private boolean isCancel;

    private int returnStatus;

}
