package com.project.WebAloTra.controller.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.dto.Product.ProductDto;
import com.project.WebAloTra.dto.Product.SearchProductDto;
import com.project.WebAloTra.entity.Product;
import com.project.WebAloTra.repository.ProductRepository;
import com.project.WebAloTra.service.CloudinaryService;
import com.project.WebAloTra.service.ProductService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
public class ProductRestController {
	private final ProductService productService;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;


    public ProductRestController(ProductService productService, ProductRepository productRepository, CloudinaryService cloudinaryService) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.cloudinaryService = cloudinaryService;
    }


    @GetMapping("/api/products")
    public Page<ProductDto> getAllProductsApi(@PageableDefault(page = 0, size = 12) Pageable pageable) {
        return productService.getAllProductApi(pageable);
    }

    @GetMapping("/api/products-no-pagination")
    public List<ProductDto> getAllProductsApi(SearchProductDto searchRequest) {
        return productService.getAllProductNoPaginationApi(searchRequest);
    }

    @GetMapping("/api/products/filter")
    public Page<ProductDto> filterProductApi(SearchProductDto searchRequest, @PageableDefault(page = 0, size = 10) Pageable page){
        com.project.WebAloTra.entity.Account account = com.project.WebAloTra.utils.UserLoginUtil.getCurrentLogin();
        if (account != null && account.getBranch() != null && 
           (account.getRole().getName() == com.project.WebAloTra.entity.enumClass.RoleName.ROLE_VENDOR || 
            account.getRole().getName() == com.project.WebAloTra.entity.enumClass.RoleName.ROLE_STAFF)) {
            return productService.searchBranchProduct(searchRequest, page, account.getBranch().getId());
        }
        return productService.searchProduct(searchRequest, page);
    }

    @GetMapping("/api/products/getByBarcode")
    public ProductDto filterProductApi(@RequestParam String barcode){
//        searchForm.setPriceStart(searchForm.getPriceStart()*1000000);
//        searchForm.setPriceEnd(searchForm.getPriceEnd()*1000000);
        return productService.getProductByBarcode(barcode);
    }

    @GetMapping("/api/products/{detailId}/productDetail")
    public ProductDto getByProductDetailId(@PathVariable Long detailId) {
        return productService.getByProductDetailId(detailId);
    }
    
    @GetMapping("/api/products/{id}/image")
    public Map<String, String> getProductImage(@PathVariable("id") Long id) {
        Product product = productRepository.findById(id).orElse(null);
        String url = "/images/item-cart-01.jpg"; // fallback
        if (product != null && product.getImage() != null && !product.getImage().isEmpty()) {
            String link = product.getImage().get(0).getLink();
            if (link != null) {
                url = (link.startsWith("http")) ? link : ("/" + link);
            }
        }
        return Collections.singletonMap("url", url);
    }

    @GetMapping("/api/products/{code}/images")
    public List<String> getProductImagesByCode(@PathVariable("code") String code) {
        Product product = productService.getProductByCode(code);
        if (product == null) return Collections.emptyList();

        List<String> urls = new ArrayList<>();
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            for (var img : product.getImage()) {
                if (img != null && img.getLink() != null) {
                    String link = img.getLink();
                    urls.add(link.startsWith("http") ? link : ("/" + link));
                }
            }
        }
        List<String> cloudUrls = cloudinaryService.listImagesByFolder("Products/" + code);
        if (cloudUrls != null && !cloudUrls.isEmpty()) {
            return cloudUrls;
        }
        return urls;
    }
}