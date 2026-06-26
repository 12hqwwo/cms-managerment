package com.project.WebAloTra.entity;

import lombok.*;
import org.hibernate.annotations.Nationalized;

import com.project.WebAloTra.entity.enumClass.PaymentMethodName;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "payment_method") // ✅ đúng với tên thật trong DB
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethod implements Serializable {
    @Id
    private Long id;

    @Nationalized
    @Enumerated(EnumType.STRING)
    private PaymentMethodName name;

    private int status;
}
