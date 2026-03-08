package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.enums.OrderStatus;
import com.gotcha.gotcha_api.exception.custom.InsufficientCoinsException;
import com.gotcha.gotcha_api.exception.custom.ProductOutOfStockException;
import com.gotcha.gotcha_api.model.*;
import com.gotcha.gotcha_api.model.dto.OrderItemRequest;
import com.gotcha.gotcha_api.model.dto.OrderItemResponse;
import com.gotcha.gotcha_api.model.dto.OrderRequest;
import com.gotcha.gotcha_api.model.dto.OrderResponse;
import com.gotcha.gotcha_api.repo.OrderRepo;
import com.gotcha.gotcha_api.repo.ProductRepo;
import com.gotcha.gotcha_api.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    ProductService productService;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    UserRepo userRepo;

    public void placeOrder(OrderRequest orderRequest,  User user) {
        Order order = new Order();
        String orderCode = "ORD" + UUID.randomUUID().toString();
        order.setOrderCode(orderCode);
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
        order.setUpdateDate(LocalDateTime.now());
        order.setUser(user);

        List<OrderItem> orderItems = new ArrayList<>();
        Long totalPrice = 0L;

        for(OrderItemRequest orderItemRequest : orderRequest.items()){
            Product product = productService.getProductByID(orderItemRequest.productId());
            if(product.getStockQuantity() < orderItemRequest.quantity()){
                throw new ProductOutOfStockException("Product " + product.getName() + " is out of stock");
            }
            product.setStockQuantity(product.getStockQuantity() - orderItemRequest.quantity());
            productRepo.save(product);

            Long itemPrice = product.getPrice() * orderItemRequest.quantity();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(orderItemRequest.quantity())
                    .totalPrice(itemPrice)
                    .build();
            orderItems.add(orderItem);
            totalPrice += itemPrice;
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);

        if (user.getGotchaCoins() == null || user.getGotchaCoins() < totalPrice) {
            throw new InsufficientCoinsException("Not enough Gotcha Coins. Required: " + totalPrice + ", Available: " + (user.getGotchaCoins() == null ? 0 : user.getGotchaCoins()));
        }

        user.setGotchaCoins(user.getGotchaCoins() - totalPrice);
        userRepo.save(user);

        user.getOrders().add(order);
        orderRepo.save(order);




    }


    public List<OrderResponse> getAllOrders(User user) {
        List<Order> orders = user.getOrders();
        List<OrderResponse> orderResponses = new ArrayList<>();
        for(Order order : orders){
            List<OrderItemResponse> orderItemResponses = new ArrayList<>();
            for(OrderItem item : order.getOrderItems()){
                OrderItemResponse orderItemResponse = new OrderItemResponse(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getTotalPrice()
                );
                orderItemResponses.add(orderItemResponse);
            }
            OrderResponse orderResponse = new OrderResponse(
                    order.getOrderCode(),
                    order.getStatus(),
                    order.getCreateDate(),
                    order.getTotalPrice(),
                    orderItemResponses
            );
            orderResponses.add(orderResponse);

        }
        return orderResponses;
    }
}
