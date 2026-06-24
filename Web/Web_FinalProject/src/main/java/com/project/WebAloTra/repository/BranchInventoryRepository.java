package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.WebAloTra.entity.Branch;
import com.project.WebAloTra.entity.BranchInventory;
import com.project.WebAloTra.entity.ProductDetail;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Long> {

    Optional<BranchInventory> findByBranchIdAndProductDetailId(Long branchId, Long productDetailId);

    List<BranchInventory> findByBranchId(Long branchId);

    List<BranchInventory> findByProductDetailId(Long productDetailId);

    List<BranchInventory> findByBranch(Branch branch);

    List<BranchInventory> findByProductDetail(ProductDetail productDetail);


    /**
     * ✅ Lấy sản phẩm còn hàng của chi nhánh (Native SQL với tên cột camelCase đúng theo DDL)
     */
    @Query(value = """
        SELECT bi.*
        FROM branch_inventory bi
        JOIN product_detail pd ON bi.product_detail_id = pd.id
        JOIN product p ON pd.product_id = p.id
        WHERE bi.branch_id = :branchId
          AND bi.quantity > 0
          AND bi.isActive = 1
        ORDER BY p.create_date DESC
    """, nativeQuery = true)
    List<BranchInventory> findActiveProductsByBranch(@Param("branchId") Long branchId);


    /**
     * ✅ Lấy sản phẩm theo danh mục của chi nhánh (JPQL)
     */
    @Query("""
        SELECT bi FROM BranchInventory bi 
        WHERE bi.branch.id = :branchId 
          AND bi.productDetail.product.category.id = :categoryId
          AND bi.quantity > 0 
          AND bi.isActive = true
    """)
    List<BranchInventory> findProductsByBranchAndCategory(
        @Param("branchId") Long branchId, 
        @Param("categoryId") Long categoryId
    );


    /**
     * ✅ Kiểm tra xem chi nhánh có sản phẩm này không và còn hàng
     */
    @Query("""
        SELECT CASE WHEN bi.quantity > 0 THEN true ELSE false END 
        FROM BranchInventory bi
        WHERE bi.branch.id = :branchId 
          AND bi.productDetail.id = :productDetailId
    """)
    boolean hasProductInStock(
        @Param("branchId") Long branchId, 
        @Param("productDetailId") Long productDetailId
    );


    /**
     * ✅ Lấy số lượng sản phẩm trong kho của chi nhánh
     */
    @Query("""
        SELECT COALESCE(bi.quantity, 0)
        FROM BranchInventory bi
        WHERE bi.branch.id = :branchId
          AND bi.productDetail.id = :productDetailId
    """)
    Integer getQuantityByBranchAndProductDetail(
        @Param("branchId") Long branchId,
        @Param("productDetailId") Long productDetailId
    );
}
