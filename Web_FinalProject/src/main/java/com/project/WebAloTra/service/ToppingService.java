// Đường dẫn: WebsocketAndWishlist/src/main/java/com/project/DuAnTotNghiep/service/ToppingService.java

package com.project.WebAloTra.service;

import java.util.List;

import com.project.WebAloTra.dto.Topping.ToppingDto;

public interface ToppingService {
    
    List<ToppingDto> getAllToppings();
    
    List<ToppingDto> getActiveToppings();
    
    ToppingDto getToppingById(Long id);
    
    ToppingDto createTopping(ToppingDto toppingDTO);
    
    ToppingDto updateTopping(Long id, ToppingDto toppingDTO);
    
    void deleteTopping(Long id);
    
    void toggleStatus(Long id);
}