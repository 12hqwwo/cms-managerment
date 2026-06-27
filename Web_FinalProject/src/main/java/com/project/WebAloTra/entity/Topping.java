package com.project.WebAloTra.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "topping")  // ← Tên bảng phải khớp với database
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Topping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String description;

    // ✅ Đổi Boolean → Integer để khớp TINYINT(1) trong MySQL
    @Column(nullable = false)
    private Integer status = 1;
}
