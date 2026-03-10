package com.gotcha.gotcha_api.repo;

import com.gotcha.gotcha_api.model.Order;
import com.gotcha.gotcha_api.model.Product;
import com.gotcha.gotcha_api.model.dto.OrderResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> , JpaSpecificationExecutor<Order> {

}
