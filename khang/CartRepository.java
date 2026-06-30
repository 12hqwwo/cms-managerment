package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.WebAloTra.entity.Cart;
import com.project.WebAloTra.entity.Product;
import com.project.WebAloTra.entity.ProductDetail;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Đã dùng VPD (Virtual Private Database), tự lọc ở tầng DB
    // List<Cart> findAllByAccount_Id(Long accountId);
    boolean existsByProductDetail_Id(Long productDetailId);
    Cart findByProductDetail_Id(Long productDetailId);
    Cart findByProductDetail(ProductDetail productDetail);
    // void deleteAllByAccount_Id(Long accountId);

}
