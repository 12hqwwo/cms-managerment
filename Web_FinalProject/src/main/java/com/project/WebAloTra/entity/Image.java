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
@Table(name = "image")
public class Image implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "link")
    private String link;

    @Column(name = "name")
    private String name;

    // 🔗 Quan hệ nhiều ảnh thuộc về 1 sản phẩm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    
 // ✅ Thêm constructor dùng cho ProductController
    public Image(Long id, String name, LocalDateTime createDate, LocalDateTime updateDate,
                 String link, String fileType, Product product) {
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.link = link;
        this.fileType = fileType;
        this.product = product;
    }
}
