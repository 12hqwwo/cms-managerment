package com.project.WebAloTra.entity;

import lombok.*;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "category")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Nationalized
    private String name;

    private int status;

    // ✅ Khớp hoàn toàn với cột [delete_flag] trong SQL
    @Column(name = "delete_flag", nullable = false)
    private Boolean deleteFlag;
}
