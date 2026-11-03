package com.bookstore.backend.repository;

import com.bookstore.backend.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Finds a book by ISBN.
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Lists all active books with their authors fetched in one query
     * (avoids N+1 when mapping to DTOs).
     */
    @EntityGraph(attributePaths = {"authors", "publisher", "category"})
    org.springframework.data.domain.Page<Book> findAllByIsActiveTrue(org.springframework.data.domain.Pageable pageable);

    /**
     * Finds a book with authors/publisher/category pre-loaded.
     */
    @EntityGraph(attributePaths = {"authors", "publisher", "category"})
    Optional<Book> findWithDetailsById(Long id);
}
