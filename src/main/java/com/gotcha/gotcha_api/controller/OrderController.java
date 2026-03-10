package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.Order;
import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.OrderRequest;
import com.gotcha.gotcha_api.model.dto.OrderResponse;
import com.gotcha.gotcha_api.model.dto.OrderSearchParameter;
import com.gotcha.gotcha_api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping("/member/order")
    public ResponseEntity<String> placeOrder(@Valid  @RequestBody OrderRequest orderRequest, @AuthenticationPrincipal UserPrincipal  userPrincipal){
        orderService.placeOrder(orderRequest, userPrincipal.getUser());
        return ResponseEntity.ok("Order placed successfully");
    }
    @GetMapping("/member/order")
    public ResponseEntity<List<OrderResponse>> getAllOrders(@AuthenticationPrincipal UserPrincipal userPrincipal){
        return new ResponseEntity<>(orderService.getAllOrders(userPrincipal.getUser()), HttpStatus.OK);
    }

    @GetMapping("/admin/order")
    public ResponseEntity<List<OrderResponse>> getAllOrders(@Valid @ModelAttribute OrderSearchParameter params){
        return new ResponseEntity<>(orderService.getAllOrders(params), HttpStatus.OK);
    }
    @GetMapping("/admin/order/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId){
        return new ResponseEntity<>(orderService.getOrderById(orderId), HttpStatus.OK);
    }
}
