package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.OrderRequest;
import com.gotcha.gotcha_api.model.dto.OrderResponse;
import com.gotcha.gotcha_api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/member/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping
    public ResponseEntity<String> placeOrder(@Valid  @RequestBody OrderRequest orderRequest, @AuthenticationPrincipal UserPrincipal  userPrincipal){
        orderService.placeOrder(orderRequest, userPrincipal.getUser());
        return ResponseEntity.ok("Order placed successfully");
    }
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(@AuthenticationPrincipal UserPrincipal userPrincipal){
        return new ResponseEntity<>(orderService.getAllOrders(userPrincipal.getUser()), HttpStatus.OK);
    }
}
