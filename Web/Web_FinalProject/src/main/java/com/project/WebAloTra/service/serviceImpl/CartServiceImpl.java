package com.project.WebAloTra.service.serviceImpl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.WebAloTra.dto.Cart.CartDto;
import com.project.WebAloTra.dto.Cart.ProductCart;
import com.project.WebAloTra.dto.Order.OrderDetailDto;
import com.project.WebAloTra.dto.Order.OrderDto;
import com.project.WebAloTra.dto.Order.ToppingOrderDto;
import com.project.WebAloTra.dto.Product.ProductDetailDto;
import com.project.WebAloTra.entity.*;
import com.project.WebAloTra.entity.enumClass.BillStatus;
import com.project.WebAloTra.entity.enumClass.InvoiceType;
import com.project.WebAloTra.entity.enumClass.PaymentMethodName;
import com.project.WebAloTra.exception.NotFoundException;
import com.project.WebAloTra.exception.ShopApiException;
import com.project.WebAloTra.repository.*;
import com.project.WebAloTra.service.CartService;
import com.project.WebAloTra.utils.RandomUtils;
import com.project.WebAloTra.utils.UserLoginUtil;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductDiscountRepository productDiscountRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final BillRepository billRepository;
    private final BillDetailRepository billDetailRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final BillDetailToppingRepository billDetailToppingRepository;
    private final BranchRepository branchRepository;
    private final BranchInventoryRepository branchInventoryRepository;

    private final AtomicLong invoiceCounter = new AtomicLong(1);

    public CartServiceImpl(
            CartRepository cartRepository,
            ProductDiscountRepository productDiscountRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            ProductRepository productRepository,
            ProductDetailRepository productDetailRepository,
            BillRepository billRepository,
            BillDetailRepository billDetailRepository,
            DiscountCodeRepository discountCodeRepository,
            PaymentRepository paymentRepository,
            PaymentMethodRepository paymentMethodRepository,
            BillDetailToppingRepository billDetailToppingRepository,
            BranchRepository branchRepository,
            BranchInventoryRepository branchInventoryRepository
    ) {
        this.cartRepository = cartRepository;
        this.productDiscountRepository = productDiscountRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.productDetailRepository = productDetailRepository;
        this.billRepository = billRepository;
        this.billDetailRepository = billDetailRepository;
        this.discountCodeRepository = discountCodeRepository;
        this.paymentRepository = paymentRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.billDetailToppingRepository = billDetailToppingRepository;
        this.branchRepository = branchRepository;
        this.branchInventoryRepository = branchInventoryRepository;
    }

    @Override
    public List<CartDto> getAllCart() {
        List<Cart> carts = cartRepository.findAll();
        List<CartDto> cartDtos = new ArrayList<>();
        carts.forEach(cart -> {
            CartDto cartDto = new CartDto();
            cartDto.setId(cart.getId());
            cartDto.setQuantity(cart.getQuantity());
            cartDto.setCreateDate(cart.getCreateDate());
        });
        return cartDtos;
    }

    @Override
    public List<CartDto> getAllCartByAccountId() {
        Account account = UserLoginUtil.getCurrentLogin();
        List<Cart> cartList = cartRepository.findAllByAccount_Id(account.getId());
        List<CartDto> cartDtos = new ArrayList<>();

        cartList.forEach(cart -> {
            Product product = productRepository.findById(cart.getProductDetail().getProduct().getId())
                    .orElseThrow();

            ProductCart productCart = new ProductCart();
            productCart.setProductId(product.getId());
            productCart.setName(product.getName());
            productCart.setCode(product.getCode());
            productCart.setDescribe(product.getDescribe());
            productCart.setImageUrl(product.getFirstImageUrl());

            ProductDetailDto productDetailDto = new ProductDetailDto();
            productDetailDto.setId(cart.getProductDetail().getId());
            productDetailDto.setProductId(product.getId());
            productDetailDto.setPrice(cart.getProductDetail().getPrice());
            productDetailDto.setSize(cart.getProductDetail().getSize());
            productDetailDto.setQuantity(cart.getProductDetail().getQuantity());
            productDetailDto.setColor(cart.getProductDetail().getColor());

            ProductDiscount productDiscount =
                    productDiscountRepository.findValidDiscountByProductDetailId(cart.getProductDetail().getId());
            if (productDiscount != null) {
                productDetailDto.setDiscountedPrice(productDiscount.getDiscountedAmount());
            }

            CartDto cartDto = new CartDto();
            cartDto.setId(cart.getId());
            cartDto.setQuantity(cart.getQuantity());
            cartDto.setCreateDate(cart.getCreateDate());
            cartDto.setAccountId(account.getId());
            cartDto.setProduct(productCart);
            cartDto.setDetail(productDetailDto);
            cartDtos.add(cartDto);
        });
        return cartDtos;
    }

    @Override
    public void addToCart(CartDto cartDto) throws NotFoundException {
        Cart cart = new Cart();
        Account account = UserLoginUtil.getCurrentLogin();
        cart.setAccount(account);

        ProductDetail productDetail = productDetailRepository.findById(cartDto.getDetail().getId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        cart.setProductDetail(productDetail);
        int quantityAdding = cartDto.getQuantity();
        int quantityRemaining = productDetail.getQuantity();

        if (cartRepository.existsByProductDetail_IdAndAccount_Id(productDetail.getId(), account.getId())) {
            Cart existsCart = cartRepository.findByProductDetail_IdAndAccount_Id(productDetail.getId(), account.getId());
            int currentQuantity = existsCart.getQuantity();
            int quantityNeedToAdd = currentQuantity + quantityAdding;

            existsCart.setQuantity(quantityNeedToAdd);
            existsCart.setUpdateDate(LocalDateTime.now());

            if (quantityRemaining == 0) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST, "Sản phẩm có thuộc tính này đã hết hàng");
            }

            if (quantityRemaining < quantityNeedToAdd) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST, "Số lượng thêm vào giỏ hàng lớn hơn số lượng tồn");
            }
            cartRepository.save(existsCart);
        } else {
            if (quantityRemaining < quantityAdding) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST, "Số lượng thêm vào giỏ hàng lớn hơn số lượng tồn");
            }

            cart.setQuantity(quantityAdding);
            cart.setCreateDate(LocalDateTime.now());
            cart.setUpdateDate(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }

    @Override
    public void updateCart(CartDto cartDto) throws NotFoundException {
        Cart cart = cartRepository.findById(cartDto.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found"));
        int quantityAdding = cartDto.getQuantity();
        int quantityRemaining = cart.getProductDetail().getQuantity();

        if (quantityAdding > quantityRemaining) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST,
                    "Xin lỗi, số lượng sản phẩm này chỉ còn: " + quantityRemaining);
        }
        cart.setQuantity(cartDto.getQuantity());
        cartRepository.save(cart);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void orderUser(OrderDto orderDto) {
        Bill billCurrent = billRepository.findTopByOrderByIdDesc();
        int nextCode = 1;
        if (billCurrent != null && billCurrent.getCode() != null) {
            try {
                String numericPart = billCurrent.getCode().replaceAll("\\D+", "");
                if (!numericPart.isEmpty()) {
                    nextCode = Integer.parseInt(numericPart) + 1;
                }
            } catch (NumberFormatException e) {
                nextCode = 1;
            }
        }
        String billCode = "HD" + String.format("%03d", nextCode);

        Bill bill = new Bill();
        bill.setBillingAddress(orderDto.getBillingAddress());
        bill.setCreateDate(LocalDateTime.now());
        bill.setUpdateDate(LocalDateTime.now());
        bill.setCode(billCode);
        bill.setInvoiceType(InvoiceType.ONLINE);
        bill.setStatus(BillStatus.CHO_XAC_NHAN);
        bill.setPromotionPrice(orderDto.getPromotionPrice());
        bill.setReturnStatus(false);

        if (orderDto.getBranchId() != null) {
            Branch branch = branchRepository.findById(orderDto.getBranchId())
                    .orElseThrow(() -> new NotFoundException("Chi nhánh không tồn tại"));
            bill.setBranch(branch);
        }

        if (UserLoginUtil.getCurrentLogin() != null) {
            Account account = UserLoginUtil.getCurrentLogin();
            bill.setCustomer(account.getCustomer());
        }

        double total = 0.0;
        List<BillDetail> billDetailList = new ArrayList<>();

        for (OrderDetailDto item : orderDto.getOrderDetailDtos()) {
            BillDetail billDetail = new BillDetail();
            billDetail.setBill(bill);
            billDetail.setQuantity(item.getQuantity());

            ProductDetail productDetail = productDetailRepository.findById(item.getProductDetailId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));
            billDetail.setProductDetail(productDetail);

            Product product = productRepository.findByProductDetail_Id(productDetail.getId());

            if ("2".equals(product.getStatus())) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST,
                        "Sản phẩm " + productDetail.getProduct().getName() + " đã ngừng bán");
            }
            if (productDetail.getQuantity() - item.getQuantity() < 0) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST,
                        "Sản phẩm " + productDetail.getProduct().getName() + " chỉ còn lại " + productDetail.getQuantity());
            }

            double toppingTotal = 0.0;
            List<BillDetailTopping> toppingEntities = new ArrayList<>();
            if (item.getToppings() != null && !item.getToppings().isEmpty()) {
                for (ToppingOrderDto toppingDto : item.getToppings()) {
                    if (toppingDto.getPrice() == null) continue;
                    BillDetailTopping toppingEntity = new BillDetailTopping();
                    toppingEntity.setToppingName(toppingDto.getName());
                    toppingEntity.setToppingPrice(toppingDto.getPrice());
                    toppingEntity.setBillDetail(billDetail);
                    toppingEntities.add(toppingEntity);
                    toppingTotal += toppingDto.getPrice();
                }
            }

            ProductDiscount productDiscount =
                    productDiscountRepository.findValidDiscountByProductDetailId(productDetail.getId());
            double productPrice = (productDiscount != null)
                    ? productDiscount.getDiscountedAmount()
                    : productDetail.getPrice();

            double unitPrice = productPrice + toppingTotal;
            billDetail.setMomentPrice(unitPrice);

            total += unitPrice * item.getQuantity();

            if (!toppingEntities.isEmpty()) {
                billDetail.setBillDetailToppings(toppingEntities);
            }

            productDetail.setQuantity(productDetail.getQuantity() - item.getQuantity());
            productDetailRepository.save(productDetail);
            billDetailList.add(billDetail);
        }

	    if (orderDto.getVoucherId() != null) {
	        DiscountCode discountCode = discountCodeRepository.findById(orderDto.getVoucherId())
	                .orElseThrow(() -> new ShopApiException(HttpStatus.BAD_REQUEST, "Không tìm thấy voucher"));
	        if (discountCode.getMaximumUsage() <= 0) {
	            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã giảm giá đã hết");
	        }
	        discountCode.setMaximumUsage(discountCode.getMaximumUsage() - 1);
	        discountCodeRepository.save(discountCode);
	        bill.setDiscountCode(discountCode);
	    }

	    // ✅ Tính promotion discount từ frontend
	    Double promotionDiscount = orderDto.getPromotionPrice();
	    if (promotionDiscount == null || Double.isNaN(promotionDiscount) || promotionDiscount < 0) {
	        promotionDiscount = 0.0;
	    }

	 // ✅ Tổng cuối cùng (đã trừ khuyến mãi)
	    double finalTotal = total - promotionDiscount;
	    if (Double.isNaN(finalTotal) || finalTotal < 0) finalTotal = 0.0;

	    bill.setBillDetail(billDetailList);
	    bill.setAmount(finalTotal); // đảm bảo đặt lại ngay trước khi lưu

	    PaymentMethod paymentMethod = paymentMethodRepository.findById(orderDto.getPaymentMethodId())
	            .orElseThrow(() -> new NotFoundException("Payment not found"));
	    bill.setPaymentMethod(paymentMethod);

	    Bill billNew = billRepository.save(bill);

	    // ✅ Thanh toán
	    if (paymentMethod.getName() == PaymentMethodName.TIEN_MAT) {
	        Payment payment = new Payment();
	        payment.setPaymentDate(LocalDateTime.now());
	        payment.setOrderStatus("1");
	        payment.setBill(billNew);
	        payment.setAmount(String.valueOf(finalTotal));
	        payment.setOrderId(RandomUtils.generateRandomOrderId(8));
	        payment.setStatusExchange(0);
	        paymentRepository.save(payment);
	    } else if (paymentMethod.getName() == PaymentMethodName.CHUYEN_KHOAN) {
	        Payment payment = paymentRepository.findByOrderId(orderDto.getOrderId());
	        payment.setBill(billNew);
	        payment.setStatusExchange(0);
	        paymentRepository.save(payment);
	    }
	    
	    // 🔹 Xóa giỏ hàng sau khi đặt hàng thành công	

	    cartRepository.deleteAllByAccount_Id(UserLoginUtil.getCurrentLogin().getId());
	}




	@Override
	@Transactional(rollbackOn = Exception.class)
	public OrderDto orderAdmin(OrderDto orderDto) {
	    Bill billCurrent = billRepository.findTopByOrderByIdDesc();
	    int nextCode = 1;
	    if (billCurrent != null && billCurrent.getCode() != null) {
	        try {
	            String numericPart = billCurrent.getCode().replaceAll("\\D+", "");
	            if (!numericPart.isEmpty()) {
	                nextCode = Integer.parseInt(numericPart) + 1;
	            }
	        } catch (NumberFormatException e) {
	            nextCode = 1;
	        }
	    }
	    String billCode = "HD" + String.format("%03d", nextCode);

	    Bill bill = new Bill();
	    bill.setBillingAddress(orderDto.getBillingAddress());
	    bill.setCreateDate(LocalDateTime.now());
	    bill.setUpdateDate(LocalDateTime.now());
	    bill.setCode(billCode);
	    bill.setInvoiceType(InvoiceType.OFFLINE);
	    bill.setStatus(BillStatus.HOAN_THANH);
	    bill.setPromotionPrice(orderDto.getPromotionPrice());
	    bill.setReturnStatus(false);
	    Account cashierAccount = UserLoginUtil.getCurrentLogin();
	    if (cashierAccount == null) {
	        throw new ShopApiException(HttpStatus.UNAUTHORIZED, "Không xác định được tài khoản thanh toán");
	    }
	    bill.setCashier(cashierAccount);

	    boolean isVendorOrStaff = cashierAccount.getRole() != null
	            && (cashierAccount.getRole().getName() == com.project.WebAloTra.entity.enumClass.RoleName.ROLE_VENDOR
	            || cashierAccount.getRole().getName() == com.project.WebAloTra.entity.enumClass.RoleName.ROLE_STAFF);

	    Long effectiveBranchId = orderDto.getBranchId();
	    if (isVendorOrStaff) {
	        if (cashierAccount.getBranch() == null) {
	            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Tài khoản nhân viên/quản lý chưa được gán chi nhánh");
	        }
	        effectiveBranchId = cashierAccount.getBranch().getId();
	    }

	    if (effectiveBranchId != null) {
	        Branch branch = branchRepository.findById(effectiveBranchId)
	            .orElseThrow(() -> new NotFoundException("Chi nhánh không tồn tại"));
	        bill.setBranch(branch);
	    } else if (isVendorOrStaff) {
	        throw new ShopApiException(HttpStatus.BAD_REQUEST, "Không tìm thấy chi nhánh hợp lệ cho nhân viên/quản lý");
	    }


	    if (orderDto.getCustomer() != null && orderDto.getCustomer().getId() != null) {
	        Customer customer = customerRepository.findById(orderDto.getCustomer().getId())
	                .orElseThrow(() -> new NotFoundException("Customer not found"));
	        bill.setCustomer(customer);
	    }

	    double total = 0.0;
	    List<BillDetail> billDetailList = new ArrayList<>();

	    for (OrderDetailDto item : orderDto.getOrderDetailDtos()) {
	        BillDetail billDetail = new BillDetail();
	        billDetail.setBill(bill);
	        billDetail.setQuantity(item.getQuantity());

	        ProductDetail productDetail = productDetailRepository.findById(item.getProductDetailId())
	                .orElseThrow(() -> new NotFoundException("Product not found"));
	        billDetail.setProductDetail(productDetail);

	        double toppingTotal = 0.0;
	        List<BillDetailTopping> toppingEntities = new ArrayList<>();
	        if (item.getToppings() != null && !item.getToppings().isEmpty()) {
	            for (ToppingOrderDto toppingDto : item.getToppings()) {
	                if (toppingDto.getPrice() == null) continue;
	                BillDetailTopping toppingEntity = new BillDetailTopping();
	                toppingEntity.setToppingName(toppingDto.getName());
	                toppingEntity.setToppingPrice(toppingDto.getPrice());
	                toppingEntity.setBillDetail(billDetail);
	                toppingEntities.add(toppingEntity);
	                toppingTotal += toppingDto.getPrice();
	            }
	        }

	        ProductDiscount productDiscount =
	                productDiscountRepository.findValidDiscountByProductDetailId(productDetail.getId());
	        double productPrice = (productDiscount != null)
	                ? productDiscount.getDiscountedAmount()
	                : productDetail.getPrice();

	        double unitPrice = productPrice + toppingTotal;
	        billDetail.setMomentPrice(unitPrice);
	        total += unitPrice * item.getQuantity();

	        if (!toppingEntities.isEmpty()) {
	            billDetail.setBillDetailToppings(toppingEntities);
	        }

	        if (productDetail.getQuantity() - item.getQuantity() < 0) {
	            throw new ShopApiException(HttpStatus.BAD_REQUEST,
	                    "Sản phẩm " + productDetail.getProduct().getName() + " chỉ còn lại " + productDetail.getQuantity());
	        }

	        if (effectiveBranchId != null) {
	            BranchInventory branchInventory = branchInventoryRepository
	                    .findByBranchIdAndProductDetailId(effectiveBranchId, productDetail.getId())
	                    .orElseThrow(() -> new ShopApiException(HttpStatus.BAD_REQUEST,
	                            "Sản phẩm " + productDetail.getProduct().getName() + " không có trong tồn kho chi nhánh"));
	            if (branchInventory.getQuantity() == null || branchInventory.getQuantity() - item.getQuantity() < 0) {
	                throw new ShopApiException(HttpStatus.BAD_REQUEST,
	                        "Sản phẩm " + productDetail.getProduct().getName() + " không đủ số lượng tại chi nhánh");
	            }
	            branchInventory.setQuantity(branchInventory.getQuantity() - item.getQuantity());
	            branchInventory.setUpdateDate(LocalDateTime.now());
	            branchInventoryRepository.save(branchInventory);
	        }

	        productDetail.setQuantity(productDetail.getQuantity() - item.getQuantity());
	        productDetailRepository.save(productDetail);
	        billDetailList.add(billDetail);
	    }

	    if (orderDto.getVoucherId() != null) {
	        DiscountCode discountCode = discountCodeRepository.findById(orderDto.getVoucherId())
	                .orElseThrow(() -> new ShopApiException(HttpStatus.BAD_REQUEST, "Không tìm thấy voucher"));
	        if (discountCode.getMaximumUsage() <= 0) {
	            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã giảm giá đã hết");
	        }
	        discountCode.setMaximumUsage(discountCode.getMaximumUsage() - 1);
	        discountCodeRepository.save(discountCode);
	        bill.setDiscountCode(discountCode);
	    }

	    double promotionDiscount = orderDto.getPromotionPrice();
	    if (Double.isNaN(promotionDiscount) || promotionDiscount < 0) {
	        promotionDiscount = 0.0;
	    }
	    double finalTotal = total - promotionDiscount;
	    if (Double.isNaN(finalTotal) || finalTotal < 0) finalTotal = 0.0;

	    bill.setAmount(finalTotal);
	    bill.setBillDetail(billDetailList);

	    PaymentMethod paymentMethod = paymentMethodRepository.findById(orderDto.getPaymentMethodId())
	            .orElseThrow(() -> new NotFoundException("Payment not found"));
	    bill.setPaymentMethod(paymentMethod);

	    Bill billNew = billRepository.save(bill);

	    Payment payment = new Payment();
	    payment.setPaymentDate(LocalDateTime.now());
	    payment.setOrderStatus("1");
	    payment.setBill(billNew);
	    payment.setAmount(String.valueOf(finalTotal));
	    payment.setStatusExchange(0);
	    payment.setOrderId(RandomUtils.generateRandomOrderId(8));
	    paymentRepository.save(payment);

	    return new OrderDto(
	    	    billNew.getId().toString(),     // billId
	    	    orderDto.getCustomer(),         // customer
	    	    billNew.getInvoiceType(),       // invoiceType
	    	    billNew.getStatus(),            // billStatus
	    	    billNew.getPaymentMethod().getId(), // paymentMethodId
	    	    billNew.getBillingAddress(),    // billingAddress
	    	    billNew.getPromotionPrice(),    // promotionPrice
	    	    null,                           // voucherId
	    	    null,                           // orderId
	    	    billNew.getBranch() != null ? billNew.getBranch().getId() : null, // ✅ branchId (trước orderDetailDtos)
	    	    null                            // ✅ orderDetailDtos cuối cùng
	    	);
	}


	@Override
	public void deleteCart(Long id) {
		cartRepository.deleteById(id);
	}


}
