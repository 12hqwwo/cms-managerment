package com.project.WebAloTra.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "color")
@Data
@AllArgsConstructor
@NoArgsConstructor
// ✅ Bỏ qua các field tự động sinh bởi Hibernate (tránh lỗi JSON)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Color implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Nationalized
    private String name;

    @Column(name = "delete_flag", nullable = false)
    private boolean deleteFlag = false;
}
