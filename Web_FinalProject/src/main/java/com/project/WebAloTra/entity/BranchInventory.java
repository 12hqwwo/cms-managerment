package com.project.WebAloTra.entity;

import lombok.*;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "branch_inventory", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"branch_id", "product_detail_id"})
})
public class BranchInventory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false)
    private ProductDetail productDetail;

    @Column(name = "quantity")
    private Integer quantity = 0;

    @Column(name = "minQuantity")
    private Integer minQuantity = 0;

    @Column(name = "maxQuantity")
    private Integer maxQuantity;

    // ✅ Đúng với tên cột trong DB: isActive (viết hoa A)
    @Column(name = "isActive")
    private boolean isActive = true;

    // ✅ Đúng với DB: createDate (viết hoa D)
    @Column(name = "createDate")
    private LocalDateTime createDate;

    // ✅ Đúng với DB: updateDate (viết hoa D)
    @Column(name = "updateDate")
    private LocalDateTime updateDate;
}
