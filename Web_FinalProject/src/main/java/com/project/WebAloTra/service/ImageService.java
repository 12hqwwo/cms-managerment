package com.project.WebAloTra.service;

import javassist.NotFoundException;
import org.springframework.stereotype.Service;

import com.project.WebAloTra.entity.Image;

import java.util.List;

@Service
public interface ImageService {
    List<Image> getAllImagesByProductId(Long productId);
    void removeImageByIds(List<Long> ids);
}
