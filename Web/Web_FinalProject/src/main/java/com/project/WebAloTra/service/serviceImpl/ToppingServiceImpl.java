// Đường dẫn: WebsocketAndWishlist/src/main/java/com/project/DuAnTotNghiep/service/serviceImpl/ToppingServiceImpl.java

package com.project.WebAloTra.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.WebAloTra.dto.Topping.ToppingDto;
import com.project.WebAloTra.entity.Topping;
import com.project.WebAloTra.exception.NotFoundException;
import com.project.WebAloTra.repository.ToppingRepository;
import com.project.WebAloTra.service.ToppingService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ToppingServiceImpl implements ToppingService {

    @Autowired
    private ToppingRepository toppingRepository;

    /**
     * Lấy tất cả topping (dành cho admin)
     */
    @Override
    public List<ToppingDto> getAllToppings() {
        return toppingRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy các topping đang hoạt động (status = 1)
     */
    @Override
    public List<ToppingDto> getActiveToppings() {
        return toppingRepository.findAllActiveToppings().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy topping theo ID
     */
    @Override
    public ToppingDto getToppingById(Long id) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topping với id: " + id));
        return convertToDTO(topping);
    }

    /**
     * Thêm mới topping
     */
    @Override
    @Transactional
    public ToppingDto createTopping(ToppingDto dto) {
        if (toppingRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Tên topping đã tồn tại");
        }

        Topping topping = new Topping();
        topping.setName(dto.getName());
        topping.setPrice(dto.getPrice());
        topping.setDescription(dto.getDescription());
        topping.setStatus(1); // ✅ status = 1 (đang hoạt động)

        Topping saved = toppingRepository.save(topping);
        return convertToDTO(saved);
    }

    /**
     * Cập nhật topping
     */
    @Override
    @Transactional
    public ToppingDto updateTopping(Long id, ToppingDto dto) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topping với id: " + id));

        topping.setName(dto.getName());
        topping.setPrice(dto.getPrice());
        topping.setDescription(dto.getDescription());

        Topping updated = toppingRepository.save(topping);
        return convertToDTO(updated);
    }

    /**
     * Xóa topping theo ID
     */
    @Override
    @Transactional
    public void deleteTopping(Long id) {
        if (!toppingRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy topping với id: " + id);
        }
        toppingRepository.deleteById(id);
    }

    /**
     * Bật/tắt trạng thái topping (1 ↔ 0)
     */
    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topping với id: " + id));

        // ✅ status = 1 → 0, hoặc 0 → 1
        topping.setStatus(topping.getStatus() != null && topping.getStatus() == 1 ? 0 : 1);
        toppingRepository.save(topping);
    }

    /**
     * Convert Entity → DTO
     */
    private ToppingDto convertToDTO(Topping topping) {
        return new ToppingDto(
                topping.getId(),
                topping.getName(),
                topping.getPrice(),
                topping.getDescription(),
                topping.getStatus() != null && topping.getStatus() == 1 // ✅ trả về true/false cho frontend
        );
    }
}
