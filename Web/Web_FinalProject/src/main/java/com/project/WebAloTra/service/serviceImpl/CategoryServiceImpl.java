package com.project.WebAloTra.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.WebAloTra.dto.Category.CategoryDto;
import com.project.WebAloTra.entity.Category;
import com.project.WebAloTra.exception.NotFoundException;
import com.project.WebAloTra.exception.ShopApiException;
import com.project.WebAloTra.repository.CategoryRepository;
import com.project.WebAloTra.repository.ProductRepository;
import com.project.WebAloTra.service.CategoryService;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @Override
    public Page<Category> getAllCategory(Pageable pageable) {
        return categoryRepository.findAllByDeleteFlagFalse(pageable);
    }
    

    @Override
    public Category createCategory(Category category) {
        if(categoryRepository.existsByCode(category.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã loại " + category.getCode() + " đã tồn tại");
        }
        category.setDeleteFlag(false);
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Category category) {
        Category existingCategory = categoryRepository.findById(category.getId()).orElseThrow(null);
        if(!existingCategory.getCode().equals(category.getCode())) {
            if(categoryRepository.existsByCode(category.getCode())) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã loại " + category.getCode() + " đã tồn tại");
            }
        }
        category.setDeleteFlag(false);
        return categoryRepository.save(category);
    }

    @Override
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy Danh mục có id " + id));

        int countActive = productRepository.countActiveProductsByCategoryId(id);
        if (countActive > 0) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST,
                "Không thể xóa Danh mục này vì đang được sử dụng trong sản phẩm còn hoạt động");
        }

        category.setDeleteFlag(true);
        categoryRepository.save(category);

        categoryRepository.delete(category);
    }

    @Override
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public CategoryDto createCategoryApi(CategoryDto categoryDto) {
        if(categoryRepository.existsByCode(categoryDto.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã loại đã tồn tại");
        }
        Category category = new Category(null, categoryDto.getCode(), categoryDto.getName(), 1, false);
        Category categoryNew = categoryRepository.save(category);
        return new CategoryDto(category.getId(), category.getCode(), category.getName());
    }
}
