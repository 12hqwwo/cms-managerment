package com.project.WebAloTra.entity;

import lombok.*;

import javax.persistence.*;

import com.project.WebAloTra.entity.enumClass.RoleName;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "role")
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private RoleName name;
    
    @Column(name = "create_date")
    private Date createDate;
    
    @Column(name = "update_date")
    private Date updateDate;
}