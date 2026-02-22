package com.gotcha.gotcha_api.model;

import com.gotcha.gotcha_api.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String password;
    @Column(nullable = false)
    private String email;
    private String status;
    private Long gotchaCoins;

    private String profilePictureKey;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<EventRSVP> rsvps;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    //    private String favoriteDrink; // ??
}
