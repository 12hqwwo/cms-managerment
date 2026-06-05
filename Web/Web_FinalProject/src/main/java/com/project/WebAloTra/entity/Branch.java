package com.project.WebAloTra.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "branch")
public class Branch implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(name = "branch_code", unique = true)
    private String branchCode;

    @Nationalized
    @Column(name = "branch_name")
    private String branchName;

    @Nationalized
    private String address;

    private String phone;
    private String email;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToOne(mappedBy = "branch", fetch = FetchType.LAZY)
    @JsonIgnore
    private Account vendorAccount;

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<BranchInventory> inventories;
}
