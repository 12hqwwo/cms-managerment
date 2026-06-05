package com.project.WebAloTra.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Nationalized
    private String name;

    @Column(name = "phone_number") // ✅ Map đúng với cột thật trong DB
    private String phoneNumber;

    private String email;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressShipping> addressShippings;

    @OneToOne(cascade = CascadeType.ALL,
            mappedBy = "customer",
            fetch = FetchType.LAZY,
            optional = true)
    private Account account;
}
