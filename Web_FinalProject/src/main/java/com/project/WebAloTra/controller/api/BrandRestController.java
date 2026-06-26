package com.project.WebAloTra.controller.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.WebAloTra.dto.Brand.BrandDto;
import com.project.WebAloTra.service.BrandService;

import javax.validation.Valid;

@RestController
public class BrandRestController {
    private final BrandService brandService;

    public BrandRestController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping("/api/brand")
    public BrandDto createBrandApi(@RequestBody @Valid BrandDto brandDto) {
        return brandService.createBrandApi(brandDto);
    }
}
