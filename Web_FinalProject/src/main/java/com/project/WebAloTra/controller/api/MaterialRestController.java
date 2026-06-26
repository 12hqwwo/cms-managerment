package com.project.WebAloTra.controller.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.WebAloTra.dto.Material.MaterialDto;
import com.project.WebAloTra.service.MaterialService;

@RestController
public class MaterialRestController {
    private final MaterialService materialService;

    public MaterialRestController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping("/api/material")
    public MaterialDto createMaterialApi(@RequestBody MaterialDto materialDto) {
        return materialService.createMaterialApi(materialDto);
    }
}
