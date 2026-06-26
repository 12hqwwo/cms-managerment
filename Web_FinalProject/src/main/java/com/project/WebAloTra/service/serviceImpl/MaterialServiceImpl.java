package com.project.WebAloTra.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.WebAloTra.dto.Material.MaterialDto;
import com.project.WebAloTra.entity.Color;
import com.project.WebAloTra.entity.Material;
import com.project.WebAloTra.exception.NotFoundException;
import com.project.WebAloTra.exception.ShopApiException;
import com.project.WebAloTra.repository.MaterialRepository;
import com.project.WebAloTra.repository.ProductRepository;
import com.project.WebAloTra.service.MaterialService;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialServiceImpl implements MaterialService {

    @Autowired
    private MaterialRepository materialRepository;
    @Autowired
    private ProductRepository productRepository;

    @Override
    public Page<Material> getAllMaterial(Pageable pageable) {
        return materialRepository.findAllByDeleteFlagFalse(pageable);
    }

    @Override
    public Material save(Material material) {
        return materialRepository.save(material);
    }

    @Override
    public Material createMaterial(Material material) {
        if(materialRepository                                                                                                                                                                           .existsByCode(material.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã loại trà " + material.getCode() + " đã tồn tại");
        }
        material.setDeleteFlag(false);
        return materialRepository.save(material);
    }

    @Override
    public Material updateMaterial(Material material) {
        Material existingMaterial = materialRepository.findById(material.getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy loại trà"));
        if(!existingMaterial.getCode().equals(material.getCode())) {
            if(materialRepository.existsByCode(material.getCode())) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã loại trà " + material.getCode() + " đã tồn tại");
            }
        }
        material.setDeleteFlag(false);
        return materialRepository.save(material);
    }

    @Override
    public void delete(Long id) {
        Material material = materialRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy Chất liệu có id " + id));

        int countActive = productRepository.countActiveProductsByMaterialId(id);
        if (countActive > 0) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST,
                "Không thể xóa Chất liệu này vì đang được sử dụng trong sản phẩm còn hoạt động");
        }

        material.setDeleteFlag(true);
        materialRepository.save(material);

        materialRepository.delete(material);
    }

    @Override
    public Optional<Material> findById(Long id) {
        return materialRepository.findById(id);
    }

    @Override
    public List<Material> getAll() {
        return materialRepository.findAll();
    }

    @Override
    public MaterialDto createMaterialApi(MaterialDto materialDto) {
        if(materialRepository.existsByCode(materialDto.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã loại trà đã tồn tại");
        }
        Material material = new Material(null, materialDto.getCode(), materialDto.getName(), 1, false);
        Material materialNew = materialRepository.save(material);
        return new MaterialDto(materialNew.getId(), material.getCode(), material.getName());
    }
}
