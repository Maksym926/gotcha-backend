package com.gotcha.gotcha_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @NotBlank(message = "Event title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDate;

    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Size(max = 255, message = "Image key must not exceed 255 characters")
    private String imageKey;
}
