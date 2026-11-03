package com.bookstore.backend.dto;

import com.bookstore.backend.entity.Book;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps Book entities to API response DTOs.
 * Kept as a plain component: for this small project a hand-written
 * mapper is clearer than adding MapStruct.
 */
@Component
public class BookMapper {

    public BookDtos.BookResponse toResponse(Book book) {
        List<BookDtos.AuthorInfo> authors = book.getAuthors().stream()
                .map(author -> new BookDtos.AuthorInfo(
                        author.getId(),
                        author.getFirstName(),
                        author.getLastName()))
                .toList();

        BookDtos.PublisherInfo publisher = book.getPublisher() == null ? null
                : new BookDtos.PublisherInfo(
                        book.getPublisher().getId(),
                        book.getPublisher().getName());

        BookDtos.CategoryInfo category = book.getCategory() == null ? null
                : new BookDtos.CategoryInfo(
                        book.getCategory().getId(),
                        book.getCategory().getName());

        return new BookDtos.BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getDescription(),
                book.getLanguage(),
                book.getPageCount(),
                book.getPublicationYear(),
                book.getPrice(),
                book.getStockQuantity(),
                book.getIsActive(),
                publisher,
                category,
                authors);
    }
}
