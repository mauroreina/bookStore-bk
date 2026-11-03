package com.bookstore.backend.dto;

import com.bookstore.backend.entity.Author;
import com.bookstore.backend.entity.Book;
import com.bookstore.backend.entity.Category;
import com.bookstore.backend.entity.Publisher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookMapperTests {

    private final BookMapper mapper = new BookMapper();

    @Test
    void mapsAllFieldsAndRelationships() {
        Book book = Book.builder()
                .id(42L).isbn("isbn").title("Title").description("Description")
                .language("es").pageCount(100).publicationYear((short) 2026)
                .price(new BigDecimal("12.50")).stockQuantity(5).isActive(false)
                .publisher(Publisher.builder().id(1L).name("Publisher").build())
                .category(Category.builder().id(2L).name("Category").build())
                .authors(Set.of(Author.builder().id(3L).firstName("First").lastName("Last").build()))
                .build();

        assertEquals(new BookDtos.BookResponse(
                42L, "isbn", "Title", "Description", "es", 100, (short) 2026,
                new BigDecimal("12.50"), 5, false,
                new BookDtos.PublisherInfo(1L, "Publisher"),
                new BookDtos.CategoryInfo(2L, "Category"),
                java.util.List.of(new BookDtos.AuthorInfo(3L, "First", "Last"))),
                mapper.toResponse(book));
    }

    @Test
    void handlesMissingOptionalRelationships() {
        var response = mapper.toResponse(Book.builder().build());

        assertNull(response.publisher());
        assertNull(response.category());
        assertTrue(response.authors().isEmpty());
    }
}
