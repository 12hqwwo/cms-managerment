package com.project.WebAloTra.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/inventory-requests")
public class AdminInventoryRequestController {

    @GetMapping
    public String showInventoryRequests() {
        return "admin/inventory-requests";
    }
}
