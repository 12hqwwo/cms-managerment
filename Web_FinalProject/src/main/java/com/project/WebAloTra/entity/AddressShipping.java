package com.project.WebAloTra.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "address_shipping") // ✅ trùng tên bảng trong DB
public class AddressShipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(name = "address", nullable = false, length = 150)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id") // ✅ trùng cột DB
    private Customer customer;
}
