package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.dto.ProductRequest;
import com.gotcha.gotcha_api.model.dto.ProductResponse;
import com.gotcha.gotcha_api.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/member/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@Valid @RequestPart("productRequest") ProductRequest productRequest, @RequestPart("productImage") MultipartFile productImage){
        productService.createProduct(productRequest, productImage);

        return new ResponseEntity<>("Product created successfully", HttpStatus.OK);
    }
}
