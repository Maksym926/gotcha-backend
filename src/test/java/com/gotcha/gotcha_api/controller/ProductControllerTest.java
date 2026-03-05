package com.gotcha.gotcha_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.model.EventRSVP;
import com.gotcha.gotcha_api.model.Product;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.LoginRequest;
import com.gotcha.gotcha_api.model.dto.ProductRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.gotcha.gotcha_api.repo.ProductRepo;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ProductRepo productRepo;

    @MockitoBean
    private S3Service s3Service;

    @Autowired
    UserRepo userRepo;

    private User testUser;
    private String jwtToken;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void setUp() throws Exception {

        testUser = new User();

        testUser.setUsername("profileuser");
        testUser.setEmail("profileuser@gotcha.com");
        testUser.setPassword(encoder.encode("password123"));
        testUser.setRole(Role.ADMIN);
        userRepo.save(testUser);

        Product testProduct = new Product();

        testProduct.setName("Tea1");
        testProduct.setDescription("the best Chinese tea");
        testProduct.setBrand("ChinaTea");
        testProduct.setPrice(new BigDecimal("100.00"));
        testProduct.setCategory("Drinks");
        testProduct.setProductAvailable(true);
        testProduct.setStockQuantity(10L);
        testProduct.setReleaseDate(LocalDateTime.now());
        productRepo.save(testProduct);

        


        when(s3Service.uploadFile(any(), anyString())).thenReturn("mocked-image-key");
        when(s3Service.generateSignedUrl(anyString())).thenReturn("https://mocked-url.com/image.jpg");

        LoginRequest loginRequest = new LoginRequest(
                "profileuser@gotcha.com",
                "password123"
        );

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        jwtToken = loginResult.getResponse().getContentAsString();
    }
    @Test
    void testCreateProduct() throws Exception {
        ProductRequest productRequest = new ProductRequest(
            "Tea",
                "the best Chinese tea",
                "ChinaTea",
                new BigDecimal("100.00"),
                "Drinks",
                true,
                10L,
                ""

        );
        MockMultipartFile emptyImage = new MockMultipartFile(
                "productImage",
                "",
                "image/jpeg",
                new byte[0]
        );
        MockPart productPart = new MockPart(
                "productRequest",
                objectMapper.writeValueAsBytes(productRequest)
        );
        productPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/admin/product")
                        .file(emptyImage)
                        .part(productPart)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(req -> { req.setMethod("POST"); return req; }))
                .andExpect(status().isCreated());




        verify(s3Service, never()).uploadFile(any(), anyString());

    }
    @Test
    void testGetProducts() throws Exception{
        mockMvc.perform(get("/api/member/product")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Tea1")));
    }
    @Test
    void testGetProductById() throws Exception{
        List<Product> products = productRepo.findAll();
        Long productId = products.get(products.size() - 1).getProductId();
        mockMvc.perform(get("/api/member/product/" + productId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Tea1")));

    }

    @Test
    void testDeleteProduct() throws Exception{
        List<Product> products = productRepo.findAll();
        Long productId = products.get(products.size() - 1).getProductId();

        mockMvc.perform(delete("/api/admin/product/" + productId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateProduct() throws Exception{
        ProductRequest productRequest = new ProductRequest(
                "NewTea",
                "the best Chinese tea",
                "ChinaTea",
                new BigDecimal("120.00"),
                "Drinks",
                true,
                10L,
                ""

        );
        MockMultipartFile emptyImage = new MockMultipartFile(
                "productImage",
                "",
                "image/jpeg",
                new byte[0]
        );
        MockPart productPart = new MockPart(
                "productRequest",
                objectMapper.writeValueAsBytes(productRequest)
        );
        productPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        List<Product> products = productRepo.findAll();
        Long productId = products.get(products.size() - 1).getProductId();

        mockMvc.perform(multipart("/api/admin/product/" + productId)
                        .file(emptyImage)
                        .part(productPart)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk());




        verify(s3Service, never()).uploadFile(any(), anyString());
    }
}
