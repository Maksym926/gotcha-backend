package com.gotcha.gotcha_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.enums.SubscriptionStatus;
import com.gotcha.gotcha_api.model.Product;
import com.gotcha.gotcha_api.model.User;
import com.gotcha.gotcha_api.model.dto.LoginRequest;
import com.gotcha.gotcha_api.model.dto.OrderItemRequest;
import com.gotcha.gotcha_api.model.dto.OrderRequest;
import com.gotcha.gotcha_api.repo.ProductRepo;
import com.gotcha.gotcha_api.repo.UserRepo;
import com.gotcha.gotcha_api.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProductRepo productRepo;

    @MockitoBean
    private S3Service s3Service;

    private Product testProduct;
    private String jwtToken;
    private String adminToken;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void setUp() throws Exception {
        when(s3Service.generateSignedUrl(anyString())).thenReturn("https://mocked-url.com/image.jpg");

        User testUser = new User();
        testUser.setUsername("orderuser");
        testUser.setEmail("orderuser@gotcha.com");
        testUser.setPassword(encoder.encode("password123"));
        testUser.setRole(Role.MEMBER);
        testUser.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        testUser.setGotchaCoins(100L);
        userRepo.save(testUser);

        testProduct = new Product();
        testProduct.setName("Matcha Latte");
        testProduct.setDescription("Delicious matcha latte");
        testProduct.setBrand("Gotcha");
        testProduct.setPrice(0L);
        testProduct.setCategory("Drinks");
        testProduct.setProductAvailable(true);
        testProduct.setStockQuantity(10L);
        testProduct.setReleaseDate(LocalDateTime.now());
        productRepo.save(testProduct);

        LoginRequest loginRequest = new LoginRequest("orderuser@gotcha.com", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        jwtToken = loginResult.getResponse().getContentAsString();

        // Create admin user
        User adminUser = new User();
        adminUser.setUsername("orderadmin");
        adminUser.setEmail("orderadmin@gotcha.com");
        adminUser.setPassword(encoder.encode("password123"));
        adminUser.setRole(Role.ADMIN);
        userRepo.save(adminUser);

        MvcResult adminLogin = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("orderadmin@gotcha.com", "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = adminLogin.getResponse().getContentAsString();
    }

    // =========================================================
    // placeOrder - POST /api/member/order
    // =========================================================

    @Test
    void testPlaceOrder_Success() throws Exception {
        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(testProduct.getProductId(), 2L))
        );

        mockMvc.perform(post("/api/member/order")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Order placed successfully"));
    }

    @Test
    void testPlaceOrder_DecreasesProductStock() throws Exception {
        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(testProduct.getProductId(), 3L))
        );

        mockMvc.perform(post("/api/member/order")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk());

        Product updated = productRepo.findById(testProduct.getProductId()).orElseThrow();
        assertThat(updated.getStockQuantity()).isEqualTo(7L); // 10 - 3 = 7
    }

    @Test
    void testPlaceOrder_OutOfStock() throws Exception {
        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(testProduct.getProductId(), 999L)) // exceeds stock of 10
        );

        mockMvc.perform(post("/api/member/order")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testPlaceOrder_EmptyItems_ValidationFails() throws Exception {
        OrderRequest orderRequest = new OrderRequest(List.of());

        mockMvc.perform(post("/api/member/order")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPlaceOrder_Unauthorized() throws Exception {
        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(testProduct.getProductId(), 1L))
        );

        mockMvc.perform(post("/api/member/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // getAllOrders - GET /api/member/order
    // =========================================================

    @Test
    void testGetAllOrders_Empty() throws Exception {
        mockMvc.perform(get("/api/member/order")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetAllOrders_WithOrders() throws Exception {
        // Place an order first
        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(testProduct.getProductId(), 2L))
        );

        mockMvc.perform(post("/api/member/order")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk());

        // Then retrieve all orders and verify the response
        mockMvc.perform(get("/api/member/order")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderCode", startsWith("ORD")))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[0].totalPrice", notNullValue()))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].productName", is("Matcha Latte")))
                .andExpect(jsonPath("$[0].items[0].quantity", is(2)));
    }

    @Test
    void testGetAllOrders_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/member/order"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllOrders_OnlyShowsCurrentUserOrders() throws Exception {
        // Create another user and log in
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@gotcha.com");
        anotherUser.setPassword(encoder.encode("password123"));
        anotherUser.setRole(Role.MEMBER);
        anotherUser.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        anotherUser.setGotchaCoins(100L);
        userRepo.save(anotherUser);

        MvcResult anotherLogin = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("another@gotcha.com", "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        String anotherToken = anotherLogin.getResponse().getContentAsString();

        // Another user places an order
        mockMvc.perform(post("/api/member/order")
                        .header("Authorization", "Bearer " + anotherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrderRequest(List.of(new OrderItemRequest(testProduct.getProductId(), 1L))))))
                .andExpect(status().isOk());

        // Original test user should still see 0 orders
        mockMvc.perform(get("/api/member/order")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // =========================================================
    // Admin: GET /api/admin/order - Get all orders with filters
    // =========================================================

    @Test
    void testAdminGetAllOrders_Success() throws Exception {
        // Place an order as member first
        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(testProduct.getProductId(), 2L))
        );
        mockMvc.perform(post("/api/member/order")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk());

        // Admin can see all orders
        mockMvc.perform(get("/api/admin/order")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderCode", startsWith("ORD")));
    }

    @Test
    void testAdminGetAllOrders_FilterByStatus() throws Exception {
        // Place an order
        mockMvc.perform(post("/api/member/order")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrderRequest(List.of(new OrderItemRequest(testProduct.getProductId(), 1L))))))
                .andExpect(status().isOk());

        // Filter by PENDING status
        mockMvc.perform(get("/api/admin/order")
                        .param("status", "PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Filter by COMPLETED status — should be empty
        mockMvc.perform(get("/api/admin/order")
                        .param("status", "COMPLETED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testAdminGetAllOrders_MemberForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/order")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // Admin: GET /api/admin/order/{orderId} - Get order by ID
    // =========================================================

    @Test
    void testAdminGetOrderById_Success() throws Exception {
        // Place an order first
        mockMvc.perform(post("/api/member/order")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrderRequest(List.of(new OrderItemRequest(testProduct.getProductId(), 1L))))))
                .andExpect(status().isOk());

        // Get the order via admin list
        MvcResult result = mockMvc.perform(get("/api/admin/order")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        // Extract order ID from response
        String response = result.getResponse().getContentAsString();
        long orderId = objectMapper.readTree(response).get(0).get("orderId").asLong();

        // Get by ID
        mockMvc.perform(get("/api/admin/order/" + orderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is((int) orderId)))
                .andExpect(jsonPath("$.orderCode", startsWith("ORD")));
    }

    @Test
    void testAdminGetOrderById_NotFound() throws Exception {
        mockMvc.perform(get("/api/admin/order/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAdminGetOrderById_MemberForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/order/1")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isForbidden());
    }
}
