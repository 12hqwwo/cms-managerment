package com.project.WebAloTra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.WebAloTra.dto.Product.ProductSearchDto;
import com.project.WebAloTra.dto.Statistic.BestSellerProduct;
import com.project.WebAloTra.dto.Statistic.ProductStatistic;
import com.project.WebAloTra.entity.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Product findByCode(String code);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    Page<Product> getAllByName(String name, Pageable pageable);

    boolean existsByCode(String code);

    Page<Product> findAllByStatus(int status, Pageable pageable);
    Page<Product> findAllByStatusAndDeleteFlag(int status, boolean delete_flag, Pageable pageable);

    // 🔍 Tìm sản phẩm theo tên
    @Query(value = """
        SELECT p.id, p.code, p.name, br.name AS brandName, mt.name AS materialName, ct.name AS categoryName
        FROM product p
        INNER JOIN brand br ON br.id = p.brand_id
        INNER JOIN material mt ON mt.id = p.material_id
        INNER JOIN category ct ON ct.id = p.category_id
        WHERE p.name LIKE CONCAT('%', :productName, '%')
          AND p.delete_flag = 0
        ORDER BY p.create_date DESC
        """, nativeQuery = true)
    Page<Product> searchProductName(String productName, Pageable pageable);

    // 🔍 Tìm kiếm nâng cao sản phẩm
    @Query(value = """
        SELECT
            p.id AS idSanPham,
            p.code AS maSanPham,
            p.name AS tenSanPham,
            b.name AS nhanHang,
            m.name AS chatLieu,
            c.name AS theLoai,
            p.status AS trangThai
        FROM product p
        LEFT JOIN brand b ON b.id = p.brand_id
        LEFT JOIN material m ON m.id = p.material_id
        LEFT JOIN category c ON c.id = p.category_id
        WHERE
            (:maSanPham IS NULL OR p.code LIKE CONCAT('%', :maSanPham, '%'))
            AND (:tenSanPham IS NULL OR p.name LIKE CONCAT('%', :tenSanPham, '%'))
            AND (:nhanHang IS NULL OR b.id = :nhanHang)
            AND (:chatLieu IS NULL OR m.id = :chatLieu)
            AND (:theLoai IS NULL OR c.id = :theLoai)
            AND (:trangThai IS NULL OR p.status = :trangThai)
            AND p.delete_flag = 0
        ORDER BY p.create_date DESC
        """, nativeQuery = true)
    Page<ProductSearchDto> listSearchProduct(String maSanPham, String tenSanPham, Long nhanHang,
                                             Long chatLieu, Long theLoai, Integer trangThai, Pageable pageable);

    // 🔍 Lấy tất cả sản phẩm (đã active)
    @Query(value = """
        SELECT
            p.id AS idSanPham,
            p.code AS maSanPham,
            p.name AS tenSanPham,
            b.name AS nhanHang,
            m.name AS chatLieu,
            c.name AS theLoai,
            p.status AS trangThai
        FROM product p
        LEFT JOIN brand b ON b.id = p.brand_id
        LEFT JOIN material m ON m.id = p.material_id
        LEFT JOIN category c ON c.id = p.category_id
        WHERE p.delete_flag = 0
        ORDER BY p.create_date DESC
        """, nativeQuery = true)
    Page<ProductSearchDto> getAll(Pageable pageable);

    Page<Product> findAllByDeleteFlagFalse(Pageable pageable);

    @Query("select p from Product p join ProductDetail pd on p.id = pd.product.id where pd.id = :productDetaiLId")
    Product findByProductDetail_Id(Long productDetaiLId);

    Product findTopByOrderByIdDesc();

    // 🔥 Top 10 sản phẩm bán chạy
    @Query(value = """
        SELECT TOP(10)
            p.id,
            p.code,
            p.name,
            (SELECT TOP(1) image.link FROM image WHERE image.product_id = p.id) AS imageUrl,
            brand.name AS brand,
            cat.name AS category,
            COALESCE(SUM(bd.quantity), 0) AS totalQuantity,
            COALESCE(SUM(bd.return_quantity), 0) AS totalQuantityReturn,
            SUM(bd.quantity * bd.moment_price) AS revenue
        FROM bill b
        JOIN bill_detail bd ON b.id = bd.bill_id
        JOIN product_detail pd ON pd.id = bd.product_detail_id
        JOIN product p ON p.id = pd.product_id
        LEFT JOIN bill_return br ON br.bill_id = b.id
        JOIN brand ON brand.id = p.brand_id
        JOIN category cat ON cat.id = p.category_id
        WHERE b.status = 'HOAN_THANH'
        GROUP BY p.id, p.code, p.name, brand.name, cat.name
        ORDER BY totalQuantity DESC
        """, nativeQuery = true)
    List<BestSellerProduct> getBestSellerProduct();

    // 🔥 Top 10 bán chạy theo ngày
    @Query(value = """
        SELECT TOP(10)
            p.id,
            p.code,
            p.name,
            (SELECT TOP(1) image.link FROM image WHERE image.product_id = p.id) AS imageUrl,
            brand.name AS brand,
            cat.name AS category,
            COALESCE(SUM(bd.quantity), 0) AS totalQuantity,
            COALESCE(SUM(bd.return_quantity), 0) AS totalQuantityReturn,
            SUM(bd.quantity * bd.moment_price) AS revenue
        FROM bill b
        JOIN bill_detail bd ON b.id = bd.bill_id
        JOIN product_detail pd ON pd.id = bd.product_detail_id
        JOIN product p ON p.id = pd.product_id
        LEFT JOIN bill_return br ON br.bill_id = b.id
        JOIN brand ON brand.id = p.brand_id
        JOIN category cat ON cat.id = p.category_id
        WHERE b.status = 'HOAN_THANH'
          AND b.create_date BETWEEN :fromDate AND :toDate
        GROUP BY p.id, p.code, p.name, brand.name, cat.name
        ORDER BY totalQuantity DESC
        """, nativeQuery = true)
    List<BestSellerProduct> getBestSellerProduct(String fromDate, String toDate);

    // 📊 Thống kê doanh thu sản phẩm
    @Query(value = """
        SELECT
            p.id,
            p.code,
            p.name,
            brand.name AS brand,
            cat.name AS category,
            COALESCE(SUM(bd.quantity), 0) AS totalQuantity,
            COALESCE(SUM(bd.return_quantity), 0) AS totalQuantityReturn,
            COALESCE(SUM(bd.quantity * bd.moment_price), 0) AS revenue
        FROM product p
        JOIN product_detail pd ON p.id = pd.product_id
        LEFT JOIN bill_detail bd ON pd.id = bd.product_detail_id
        LEFT JOIN bill b ON bd.bill_id = b.id
        LEFT JOIN brand ON brand.id = p.brand_id
        LEFT JOIN category cat ON cat.id = p.category_id
        WHERE (b.status = 'HOAN_THANH' AND b.create_date BETWEEN :fromDate AND :toDate OR b.status IS NULL)
        GROUP BY p.id, p.code, p.name, brand.name, cat.name
ORDER BY MAX(p.create_date) DESC
        """, nativeQuery = true)
    List<ProductStatistic> getStatisticProduct(String fromDate, String toDate);
    @Query("SELECT COUNT(pd) FROM ProductDetail pd WHERE pd.size.id = :sizeId AND pd.product.deleteFlag = false")
    int countActiveProductsBySizeId(Long sizeId);
    
	@Query("SELECT COUNT(p) FROM Product p WHERE p.color.id = :colorId AND p.deleteFlag = 0")
	int countActiveProductsByColorId(Long colorId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.deleteFlag = 0")
    int countActiveProductsByCategoryId(Long categoryId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.material.id = :materialId AND p.deleteFlag = 0")
    int countActiveProductsByMaterialId(Long materialId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId AND p.deleteFlag = 0")
    int countActiveProductsByBrandId(Long brandId);
    
    
}
