package com.gotcha.gotcha_api.repo;

import com.gotcha.gotcha_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByStripeCustomerId(String stripeCustomerId);
}
