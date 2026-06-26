package com.project.WebAloTra.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "discount_code")
public class DiscountCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    private String code;

    @Nationalized
    private String detail;

    private int type;

    @Column(name = "maximum_amount", nullable = true)
    private Integer maximumAmount;

    @Column(name = "percentage", nullable = true)
    private Integer percentage;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "discount_amount", nullable = true)
    private Double discountAmount;

    @Column(name = "minimum_amount_in_cart")
    private Double minimumAmountInCart;

    @Column(name = "maximum_usage")
    private Integer maximumUsage;

    private int status;

    @Column(name = "delete_flag", nullable = false)
    private boolean deleteFlag = false;

	
}
