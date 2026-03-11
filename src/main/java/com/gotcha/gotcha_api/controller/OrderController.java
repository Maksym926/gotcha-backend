package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.Order;
import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.OrderRequest;
import com.gotcha.gotcha_api.model.dto.OrderResponse;
import com.gotcha.gotcha_api.model.dto.OrderSearchParameter;
import com.gotcha.gotcha_api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<Page<OrderResponse>> getAllOrders(@AuthenticationPrincipal UserPrincipal userPrincipal, @PageableDefault(size = 20, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable){
        return new ResponseEntity<>(orderService.getAllOrders(userPrincipal.getUser(), pageable), HttpStatus.OK);
    }

    @GetMapping("/admin/order")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(@Valid @ModelAttribute OrderSearchParameter params,  @PageableDefault(size = 20, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable){
        return new ResponseEntity<>(orderService.getAllOrders(params, pageable), HttpStatus.OK);
    }
    @GetMapping("/admin/order/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId){
        return new ResponseEntity<>(orderService.getOrderById(orderId), HttpStatus.OK);
    }
}
