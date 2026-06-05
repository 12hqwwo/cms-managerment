package com.project.WebAloTra.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "bill_detail")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "moment_price")
    private Double momentPrice;

    @Column(name = "return_quantity")
    private Integer returnQuantity;

    // ✅ Liên kết đến sản phẩm cụ thể trong kho
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id") // sửa cho khớp SQL
    private ProductDetail productDetail;

    // ✅ Liên kết đến hóa đơn tổng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id") // sửa cho khớp SQL
    private Bill bill;

    // ✅ Danh sách topping cho từng sản phẩm trong hóa đơn
    @OneToMany(mappedBy = "billDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillDetailTopping> billDetailToppings;
}

