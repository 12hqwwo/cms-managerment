// Đường dẫn: WebsocketAndWishlist/src/main/java/com/project/DuAnTotNghiep/controller/ToppingController.java

package com.project.WebAloTra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.dto.Topping.ToppingDto;
import com.project.WebAloTra.service.ToppingService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/toppings")
@CrossOrigin(
	    originPatterns = {"http://localhost:8080", "http://127.0.0.1:8080", "http://localhost:5173"},
	    allowCredentials = "true"
	)public class AdminToppingController {
    
    @Autowired
    private ToppingService toppingService;
    
    @GetMapping
    public ResponseEntity<List<ToppingDto>> getAllToppings() {
        return ResponseEntity.ok(toppingService.getAllToppings());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ToppingDto> getToppingById(@PathVariable Long id) {
        return ResponseEntity.ok(toppingService.getToppingById(id));
    }
    
    @PostMapping
    public ResponseEntity<ToppingDto> createTopping(@RequestBody ToppingDto toppingDTO) {
        return new ResponseEntity<>(toppingService.createTopping(toppingDTO), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ToppingDto> updateTopping(@PathVariable Long id, @RequestBody ToppingDto toppingDTO) {
        return ResponseEntity.ok(toppingService.updateTopping(id, toppingDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopping(@PathVariable Long id) {
        toppingService.deleteTopping(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        toppingService.toggleStatus(id);
        return ResponseEntity.ok().build();
    }
}