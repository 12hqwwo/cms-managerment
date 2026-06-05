package com.project.WebAloTra.service;

import org.springframework.stereotype.Service;

import com.project.WebAloTra.dto.Cart.CartDto;
import com.project.WebAloTra.dto.Order.OrderDto;
import com.project.WebAloTra.exception.NotFoundException;

import java.util.List;

@Service
public interface CartService {
//    Page<Cart> carts(Pageable pageable);
    List<CartDto> getAllCart();
    List<CartDto> getAllCartByAccountId();
    void addToCart(CartDto cartDto) throws NotFoundException;

    void updateCart(CartDto cartDto) throws NotFoundException;

    void orderUser(OrderDto orderDto);
    OrderDto orderAdmin(OrderDto orderDto);

    void deleteCart(Long id);
}
