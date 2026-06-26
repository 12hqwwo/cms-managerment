package com.project.WebAloTra.controller.api;


import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.dto.Size.SizeDto;
import com.project.WebAloTra.entity.Color;
import com.project.WebAloTra.entity.Size;
import com.project.WebAloTra.exception.NotFoundException;
import com.project.WebAloTra.service.SizeService;

import java.util.List;

@RestController
public class SizeRestController {
    private final SizeService sizeService;

    public SizeRestController(SizeService sizeService) {
        this.sizeService = sizeService;
    }

    @GetMapping("/sizes/{productId}/product")
    public List<Size> getColorsByProductId(@PathVariable Long productId) throws NotFoundException {
        return sizeService.getSizesByProductId(productId);
    }

    @GetMapping("/sizes/{productId}/product/{colorId}/color")
    public List<Size> getSizesByProductIdAndColorId(@PathVariable Long productId, @PathVariable Long colorId) throws NotFoundException {
        return sizeService.getSizesByProductIdAndColorId(productId, colorId);
    }

    @PostMapping("/api/size")
    public SizeDto createSizeApi(@RequestBody SizeDto sizeDto) {
        return sizeService.createSizeApi(sizeDto);
    }
}