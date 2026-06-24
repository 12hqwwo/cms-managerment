package com.project.WebAloTra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.WebAloTra.entity.Product;
import com.project.WebAloTra.entity.ProductDetail;
import com.project.WebAloTra.entity.ProductDiscount;

import java.util.List;

public interface ProductDiscountRepository extends JpaRepository<ProductDiscount, Long> {
    Page<ProductDiscount> findAllByProductDetailNotNull(Pageable pageable);
    List<ProductDiscount> findAllByProductDetailIn(List<ProductDetail> productDetails);
    ProductDiscount findByProductDetail_Id(Long productDetailId);
    @Query(value = "SELECT * FROM product_discount pd " +
            "WHERE pd.product_detail_id = :productDetailId " +
            "AND SYSDATE BETWEEN pd.startDate AND pd.endDate " +
            "AND pd.closed = 0", nativeQuery = true)
    ProductDiscount findValidDiscountByProductDetailId(@Param("productDetailId") Long productDetailId);
}
