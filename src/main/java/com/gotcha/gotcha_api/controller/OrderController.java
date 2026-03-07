package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.UserPrincipal;
import com.gotcha.gotcha_api.model.dto.OrderRequest;
import com.gotcha.gotcha_api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/member/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest orderRequest, @AuthenticationPrincipal UserPrincipal  userPrincipal){
        orderService.placeOrder(orderRequest, userPrincipal);
        return ResponseEntity.ok("Order placed successfully");
    }
}
