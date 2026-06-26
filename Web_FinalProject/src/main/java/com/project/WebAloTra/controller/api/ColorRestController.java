package com.project.WebAloTra.controller.api;

import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.dto.Color.ColorDto;
import com.project.WebAloTra.entity.Color;
import com.project.WebAloTra.exception.NotFoundException;
import com.project.WebAloTra.service.ColorService;

import java.util.List;

@RestController
public class ColorRestController {
    private final ColorService colorService;

    public ColorRestController(ColorService colorService) {
        this.colorService = colorService;
    }

    @PostMapping("/api/color")
    public ColorDto createColorApi(@RequestBody ColorDto colorDto) {
        return colorService.createColorApi(colorDto);
    }

    @GetMapping("/colors/{productId}/product")
    public List<Color> getColorsByProductId(@PathVariable Long productId) throws NotFoundException {
        return colorService.getColorByProductId(productId);
    }

    @GetMapping("/colors/{productId}/product/{sizeId}/size")
    public List<Color> getColorsByProductIdAndSizeId(@PathVariable Long productId, @PathVariable Long sizeId) throws NotFoundException {
        return colorService.getColorsByProductIdAndSizeId(productId, sizeId);
    }
}
