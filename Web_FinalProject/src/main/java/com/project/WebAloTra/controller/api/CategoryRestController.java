package com.project.WebAloTra.controller.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.WebAloTra.dto.Category.CategoryDto;
import com.project.WebAloTra.service.CategoryService;

@RestController
public class CategoryRestController {
    private final CategoryService categoryService;

    public CategoryRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/api/category")
    public CategoryDto createCategoryApi(@RequestBody CategoryDto categoryDto) {
        return categoryService.createCategoryApi(categoryDto);
    }
}
