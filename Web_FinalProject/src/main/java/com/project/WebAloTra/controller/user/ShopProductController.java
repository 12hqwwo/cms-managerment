package com.project.WebAloTra.controller.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.dto.Cart.CartDto;
import com.project.WebAloTra.dto.Cart.GuestCartDto;
import com.project.WebAloTra.dto.Product.ProductDetailDto;
import com.project.WebAloTra.dto.Product.ProductDto;
import com.project.WebAloTra.dto.Product.SearchProductDto;
import com.project.WebAloTra.entity.*;
import com.project.WebAloTra.exception.NotFoundException;
import com.project.WebAloTra.service.*;

import javax.servlet.http.HttpSession; // hoặc jakarta.servlet.http.HttpSession
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

@Controller
public class ShopProductController {
	private final ProductService productService;
	private final SizeService sizeService;
	private final ColorService colorService;
	private final ProductDetailService productDetailService;
	private final CategoryService categoryService;

	public ShopProductController(ProductService productService, SizeService sizeService, ColorService colorService,
			ProductDetailService productDetailService, CategoryService categoryService) {
		this.productService = productService;
		this.sizeService = sizeService;
		this.colorService = colorService;
		this.productDetailService = productDetailService;
		this.categoryService = categoryService;
	}

	@GetMapping("/getproduct")
	public String getProduct(Model model, SearchProductDto searchProductDto,
			@PageableDefault(size = 18) Pageable pageable) {

		List<Category> categories = categoryService.getAll();
		Page<ProductDto> products = productService.searchProduct(searchProductDto, pageable);

		if (searchProductDto != null) {
			int pageNumber = pageable.getPageNumber();
			int pageSize = pageable.getPageSize();
			Sort sort = pageable.getSort();
//            String url = "&size=" + pageSize;
			String url = "";

			if (searchProductDto.getKeyword() != null) {
				url += "&keyword=" + searchProductDto.getKeyword();
			}

			if (sort.isSorted()) {
				List<Sort.Order> orders = sort.toList();

				// Tạo một danh sách để lưu trữ chuỗi sắp xếp cho mỗi trường
				List<String> sortStrings = new ArrayList<>();

				for (Sort.Order order : orders) {
					// Lấy tên trường (field)
					String property = order.getProperty();

					// Kiểm tra xem có phải là sắp xếp giảm dần không
					boolean isDescending = order.isDescending();

					// Tạo chuỗi sắp xếp dạng "field,asc" hoặc "field,desc"
					String sortString = property + "," + (isDescending ? "desc" : "asc");

					// Thêm chuỗi sắp xếp vào danh sách
					sortStrings.add(sortString);
				}
				url += "&sort=" + String.join(",", sortStrings);
				searchProductDto.setSort(String.join(",", sortStrings));
			}

			if (searchProductDto.getMinPrice() != null) {
				url += "&minPrice=" + searchProductDto.getMinPrice();
			}
			if (searchProductDto.getMinPrice() != null) {
				url += "&maxPrice=" + searchProductDto.getMaxPrice();
			}
			if (searchProductDto.getCategoryId() != null) {
				url += "&category=" + searchProductDto.getCategoryId().stream().map(Object::toString) // Chuyển đổi mỗi
																										// số thành
																										// chuỗi
						.collect(Collectors.joining(","));
			}
			if (searchProductDto.getGender() != null) {
				url += "&gender=" + searchProductDto.getGender();
			}
			model.addAttribute("url", url);
		}

		model.addAttribute("products", products);
		model.addAttribute("categories", categories);
		model.addAttribute("dataFilter", searchProductDto);
		return "user/shop-product";
	}

//    private ProductDto mapToDto(Product product) {
//        ProductDto productDto = new ProductDto();
//        productDto.setId(product.getId());
//        productDto.setName(product.getName());
//        productDto.setCode(product.getCode());
//        productDto.setBrandId(product.getBrand().getId());
//        productDto.setMaterialId(product.getMaterial().getId());
//        productDto.setCreateDate(product.getCreateDate());
//        productDto.setUpdatedDate(product.getUpdatedDate());
//        productDto.setImageUrl(product.getImage().get(0).getLink());
//    }

	@GetMapping("/getproduct/search")
	public String getProductSearch(Model model, Pageable pageable, SearchProductDto searchDto) {
		Page<ProductDto> products = productService.searchProduct(searchDto, pageable);
		model.addAttribute("products", products);
		return "user/shop-product";
	}

	@GetMapping("/product-detail/{productCode}")
	public String getProductDetail(Model model, @PathVariable String productCode) {
		Product product = productService.getProductByCode(productCode);
		if (product == null) {
			return "/error/404";
		}
		model.addAttribute("product", product);
		return "user/product-detail";
	}

	@ResponseBody
	@GetMapping("/productDetails/{productId}/product")
	public List<ProductDetailDto> getProductDetailJson(@PathVariable Long productId) throws NotFoundException {
		List<ProductDetailDto> productDetails = productDetailService.getByProductId(productId);
		return productDetails;
	}

	@ModelAttribute("listSizes")
	public List<Size> getSize() {
		return sizeService.getAll();
	}

	@ModelAttribute("listColors")
	public List<Color> getColor() {
		return colorService.findAll();
	}

	// ====================== THÊM CHO GUEST ======================
	@ResponseBody
	@PostMapping("/add-to-cart-guest")
	public String addToCartGuest(@RequestParam("productId") Long productId, @RequestParam("quantity") int quantity,
			HttpSession session) {

		// ✅ Lấy giỏ hàng trong session (nếu chưa có thì tạo mới)
		List<GuestCartDto> guestCart = (List<GuestCartDto>) session.getAttribute("guestCart");
		if (guestCart == null) {
			guestCart = new ArrayList<>();
		}

		// ✅ Kiểm tra sản phẩm đã tồn tại trong giỏ chưa
		boolean found = false;
		for (GuestCartDto item : guestCart) {
			if (item.getProductId().equals(productId)) {
				item.setQuantity(item.getQuantity() + quantity);
				found = true;
				break;
			}
		}

		// ✅ Nếu chưa có thì thêm mới
		if (!found) {
			GuestCartDto newItem = new GuestCartDto();
			newItem.setProductId(productId);

			try {
				Product product = productService.getProductById(productId)
						.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));
				newItem.setName(product.getName());
				// Lấy ảnh đầu tiên nếu có
				if (product.getImage() != null && !product.getImage().isEmpty()) {
					newItem.setImageUrl(product.getImage().get(0).getLink());
				} else {
					newItem.setImageUrl("/images/default-product.png");
				}
				newItem.setPrice(product.getPrice());
			} catch (Exception e) {
				newItem.setName("Sản phẩm #" + productId);
				newItem.setPrice(0.0);
				newItem.setImageUrl("/images/default-product.png");
			}

			newItem.setQuantity(quantity);
			guestCart.add(newItem);
		}

		// ✅ Lưu lại vào session
		session.setAttribute("guestCart", guestCart);

		return "Đã thêm vào giỏ hàng (Guest)";
	}
}
