package com.gotcha.gotcha_api.repo;

import com.gotcha.gotcha_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepo extends JpaRepository<Product, Long> {
}
