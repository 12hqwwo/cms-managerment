package com.project.WebAloTra.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "bill_detail_topping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDetailTopping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Liên kết đúng khóa ngoại trong SQL Server
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_detail_id")
    private BillDetail billDetail;

    // ✅ Đặt tên cột khớp với DB
    @Column(name = "topping_name")
    private String toppingName;

    @Column(name = "topping_price")
    private Double toppingPrice;
}
