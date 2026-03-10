package com.gotcha.gotcha_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gotcha.gotcha_api.enums.AccountStatus;
import com.gotcha.gotcha_api.enums.Role;
import com.gotcha.gotcha_api.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    @JsonIgnore
    private String password;
    @Column(nullable = false)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatus status;
    private Long gotchaCoins;

    private String profilePictureKey;
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<EventRSVP> rsvps = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String mood;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;

    private String gotchaFavDrink;

    private String gotchaFavDrinkPictureKey;

    private String stripeCustomerId;

    private String stripeSubscriptionId;

}
