package com.project.WebAloTra.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.WebAloTra.dto.Color.ColorDto;
import com.project.WebAloTra.entity.Color;
import com.project.WebAloTra.entity.Product;
import com.project.WebAloTra.entity.Size;
import com.project.WebAloTra.exception.NotFoundException;
import com.project.WebAloTra.exception.ShopApiException;
import com.project.WebAloTra.repository.ColorRepo;
import com.project.WebAloTra.repository.ProductRepository;
import com.project.WebAloTra.repository.SizeRepository;
import com.project.WebAloTra.service.ColorService;

import java.util.List;
import java.util.Optional;

@Service
public class ColorServiceIpml implements ColorService {

    @Autowired
    private ColorRepo colorRepo;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Color updateColor(Color color) {
        Color existingColor = colorRepo.findById(color.getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy % đường có mã " + color.getCode()) );
        if(!existingColor.getCode().equals(color.getCode())) {
            if(colorRepo.existsByCode(color.getCode())) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã % đường " + color.getCode() + " đã tồn tại");
            }
        }
        color.setDeleteFlag(false);
        return colorRepo.save(color);
    }

    @Override
    public Color createColor(Color color) {
        if(colorRepo.existsByCode(color.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã % đường " + color.getCode() + " đã tồn tại");
        }
        color.setDeleteFlag(false);
        return colorRepo.save(color);
    }

    @Override
    public void delete(Long id) {
        Color color = colorRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy Màu có id " + id));

        int countActive = productRepository.countActiveProductsByColorId(id);
        if (countActive > 0) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST,
                "Không thể xóa Màu này vì đang được sử dụng trong sản phẩm còn hoạt động");
        }

        color.setDeleteFlag(true);
        colorRepo.save(color);

        colorRepo.delete(color);
    }

    @Override
    public List<Color> findAll() {
        return colorRepo.findAll();
    }

    @Override
    public Optional<Color> findById(Long id) {
        return colorRepo.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return colorRepo.existsById(id);
    }

    @Override
    public Page<Color> findAll(Integer page, Integer limit) {
        Pageable pageable= PageRequest.of(page,limit);
        return colorRepo.findAll(pageable);
    }

    @Override
    public Page<Color> findAll(Pageable pageable) {
        return colorRepo.findAllByDeleteFlagFalse(pageable);
    }

    @Override
    public List<Color> getColorByProductId(Long productId) throws NotFoundException {
        Product product = productRepository.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));
        return colorRepo.findColorsByProduct(product);
    }

    @Override
    public List<Color> getColorsByProductIdAndSizeId(Long productId, Long sizeId) throws NotFoundException {
        Product product = productRepository.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));
        Size size = sizeRepository.findById(sizeId).orElseThrow(() -> new NotFoundException("Size not found"));
        return colorRepo.findColorsByProductAndSize(product, size);
    }

    @Override
    public ColorDto createColorApi(ColorDto colorDto) {
        if(colorRepo.existsByCode(colorDto.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã % đường đã tồn tại");
        }

        Color color = new Color(null, colorDto.getCode(), colorDto.getName(), false);
        Color colorNew = colorRepo.save(color);
        return new ColorDto(colorNew.getId(), colorNew.getCode(), colorNew.getName());
    }
}
