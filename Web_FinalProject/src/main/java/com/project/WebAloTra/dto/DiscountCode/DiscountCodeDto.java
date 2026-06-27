package com.project.WebAloTra.dto.DiscountCode;

import lombok.Data;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import com.project.WebAloTra.entity.enumClass.DiscountCodeType;

import java.util.Date;

@Data
public class DiscountCodeDto {
    private Long id;
    private String code;
    private String detail;
    private Integer type;
    private Integer percentage;
    private Integer maximumAmount;
    private int status;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
    private Double discountAmount;
    private Double minimumAmountInCart;
    private int maximumUsage;
}
