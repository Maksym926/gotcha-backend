package com.gotcha.gotcha_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "static_content")
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


    private String imageKey;

    private String secondaryImageKey;
}
