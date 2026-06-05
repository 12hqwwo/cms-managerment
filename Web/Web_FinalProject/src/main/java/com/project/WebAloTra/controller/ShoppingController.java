package com.project.WebAloTra.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/shopping")
public class ShoppingController {

    /**
     * Trang chọn chi nhánh và mua hàng
     * GET /shopping/by-branch?customerId=1
     */
    @GetMapping("/by-branch")
    public String shoppingByBranch(@RequestParam(value = "customerId", defaultValue = "1") Long customerId, Model model) {
        model.addAttribute("customerId", customerId);
        return "customer/shopping-with-branch";
    }
}
