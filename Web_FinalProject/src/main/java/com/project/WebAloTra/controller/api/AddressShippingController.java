package com.project.WebAloTra.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.dto.AddressShipping.AddressShippingDto;
import com.project.WebAloTra.entity.AddressShipping;
import com.project.WebAloTra.service.AddressShippingService;

@Controller
public class AddressShippingController {

    private final AddressShippingService addressShippingService;

    public AddressShippingController(AddressShippingService addressShippingService) {
        this.addressShippingService = addressShippingService;
    }

    @ResponseBody
    @PostMapping("api/public/addressShipping")
    public ResponseEntity<AddressShippingDto> createAddressShipping(@RequestBody AddressShippingDto addressShippingDto){
        return ResponseEntity.ok(addressShippingService.saveAddressShippingUser(addressShippingDto));
    }

    @ResponseBody
    @DeleteMapping("/api/deleteAddress/{id}")
    public void deleteAddressShipping(@PathVariable Long id) {
        addressShippingService.deleteAddressShipping(id);
    }
}
