package com.gotcha.gotcha_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaticContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;

    @NotBlank(message = "Section key is required")
    @Size(min = 2, max = 100, message = "Section key must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Section key must contain only letters, numbers, underscores, and hyphens")
    private String sectionKey;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotBlank(message = "Image key is required")
    @Size(max = 255, message = "Image key must not exceed 255 characters")
    private String imageKey;
}
