package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.WebAloTra.entity.Image;
import com.project.WebAloTra.entity.Product;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findAllByProduct(Product product);
    Image findImageById(Long id);
}