package com.project.WebAloTra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.WebAloTra.dto.CustomerDto.CustomerDto;
import com.project.WebAloTra.entity.Customer;
import com.project.WebAloTra.entity.Product;
import com.project.WebAloTra.service.CustomerService;
import com.project.WebAloTra.service.ProductService;
import com.project.WebAloTra.service.BranchIsolationService;

@Controller
public class BuyAtTheCounterController {

    @Autowired
    ProductService productService;

    @Autowired
    CustomerService customerService;

    @Autowired
    BranchIsolationService branchIsolationService;

    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN', 'STAFF')")
    @GetMapping("/admin/pos")
    public String getIndex(Model model) {
        Long branchId = branchIsolationService.getCurrentBranchId();
        
        // Pseudo-VPD: Yêu cầu VENDOR và STAFF phải thuộc một chi nhánh để bán hàng
        if (!branchIsolationService.isAdmin() && branchId == null) {
            return "redirect:/error?msg=NoBranchAssigned";
        }
        
        // TODO: Chuyển đổi sang productService.getAllProductByBranchId(branchId, pageable1)
        // để mô phỏng VPD trước khi áp dụng Oracle VPD thực sự.
        Pageable pageable1 = PageRequest.of(0, 2);
        Page<Product> productPage = productService.getAllProduct(pageable1);
        model.addAttribute("products", productPage);
        return "admin/BuyAtTheCounter";
    }

}
