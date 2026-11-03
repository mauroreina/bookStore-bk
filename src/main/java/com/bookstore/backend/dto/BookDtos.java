package com.bookstore.backend.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request and response DTOs for the Book API. Records keep them immutable
 * and compact; mapping happens in BookMapper.
 */
public final class BookDtos {

    private BookDtos() {
    }

    public record CreateBookRequest(
            @NotBlank @Size(max = 20) String isbn,
            @NotBlank @Size(max = 255) String title,
            String description,
            @Size(max = 50) @Pattern(regexp = ".*\\S.*") String language,
            @PositiveOrZero Integer pageCount,
            @PositiveOrZero Short publicationYear,
            @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
            @PositiveOrZero Integer stockQuantity,
            Boolean isActive,
            @Positive Long publisherId,
            @Positive Long categoryId,
            List<@NotNull @Positive Long> authorIds
    ) {
    }

    public record AuthorInfo(Long id, String firstName, String lastName) {
    }

    public record CategoryInfo(Long id, String name) {
    }

    public record PublisherInfo(Long id, String name) {
    }

    public record BookResponse(
            Long id,
            String isbn,
            String title,
            String description,
            String language,
            Integer pageCount,
            Short publicationYear,
            BigDecimal price,
            Integer stockQuantity,
            Boolean isActive,
            PublisherInfo publisher,
            CategoryInfo category,
            java.util.List<AuthorInfo> authors
    ) {
    }
}
