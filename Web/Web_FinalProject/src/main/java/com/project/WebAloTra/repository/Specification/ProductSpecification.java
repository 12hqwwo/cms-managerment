package com.project.WebAloTra.repository.Specification;

import org.springframework.data.jpa.domain.Specification;

import com.project.WebAloTra.dto.Product.SearchProductDto;
import com.project.WebAloTra.entity.Product;
import com.project.WebAloTra.entity.ProductDetail;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification implements Specification<Product> {
    private SearchProductDto searchProductDto;

    public ProductSpecification(SearchProductDto searchRequest) {
        this.searchProductDto = searchRequest;
    }

    @Override
    public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if (searchProductDto.getMinPrice() != null) {
            // Tạo một subquery để lấy giá của ProductDetail đầu tiên
            Subquery<Double> subquery = query.subquery(Double.class);
            Root<Product> productRoot = subquery.from(Product.class);
            Join<Product, ProductDetail> productDetailJoin = productRoot.join("productDetails");
            subquery.select(criteriaBuilder.min(productDetailJoin.get("price")));
            subquery.where(criteriaBuilder.equal(productRoot.get("id"), root.get("id")));
            Predicate predicate = criteriaBuilder.greaterThanOrEqualTo(subquery, searchProductDto.getMinPrice());

            predicates.add(predicate);
        }

        if (searchProductDto.getMaxPrice() != null) {
            // Tạo một subquery để lấy giá của ProductDetail đầu tiên
            Subquery<Double> subquery = query.subquery(Double.class);
            Root<Product> productRoot = subquery.from(Product.class);
            Join<Product, ProductDetail> productDetailJoin = productRoot.join("productDetails");
            subquery.select(criteriaBuilder.min(productDetailJoin.get("price")));
            subquery.where(criteriaBuilder.equal(productRoot.get("id"), root.get("id")));
            Predicate predicate = criteriaBuilder.lessThanOrEqualTo(subquery, searchProductDto.getMaxPrice());

            predicates.add(predicate);

        }

        if (searchProductDto.getProductName() != null) {
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + searchProductDto.getProductName().toLowerCase() + "%"));
        }

        if (searchProductDto.getCode() != null) {
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("code")),
                    "%" + searchProductDto.getCode().toLowerCase() + "%"));
        }

        if (searchProductDto.getKeyword() != null) {
            String keyword = searchProductDto.getKeyword().toLowerCase();

            Predicate productNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                    "%" + keyword + "%");
            Predicate productCodePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("code")),
                    "%" + keyword + "%");
            Predicate combinedPredicate = criteriaBuilder.or(productNamePredicate, productCodePredicate);

            predicates.add(combinedPredicate);
        }

        if (searchProductDto.getCategoryId() != null) {
            Predicate predicate = root.get("category").get("id").in(searchProductDto.getCategoryId());
            predicates.add(predicate);
        }

        if (searchProductDto.getGender() != null) {
            Predicate predicate = criteriaBuilder.equal(root.get("gender"), searchProductDto.getGender());
            predicates.add(predicate);
        }

        predicates.add(criteriaBuilder.equal(root.get("status"), 1));
        predicates.add(criteriaBuilder.equal(root.get("deleteFlag"), false));
        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
