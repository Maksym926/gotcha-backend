package com.gotcha.gotcha_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;
    @Column(nullable = false)
    private String title;
    private String description;
    @Column(nullable = false)
    private LocalDateTime eventDate;
    private String location;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private String imageKey;




}
