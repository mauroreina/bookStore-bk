package com.bookstore.backend.service;

import com.bookstore.backend.dto.BookDtos.BookResponse;
import com.bookstore.backend.dto.BookDtos.CreateBookRequest;
import com.bookstore.backend.dto.BookMapper;
import com.bookstore.backend.entity.Author;
import com.bookstore.backend.entity.Book;
import com.bookstore.backend.exception.ResourceNotFoundException;
import com.bookstore.backend.repository.AuthorRepository;
import com.bookstore.backend.repository.BookRepository;
import com.bookstore.backend.repository.CategoryRepository;
import com.bookstore.backend.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        if (bookRepository.findByIsbn(request.isbn()).isPresent()) {
            throw new DataIntegrityViolationException("A book with this ISBN already exists");
        }

        Book book = Book.builder()
                .isbn(request.isbn())
                .title(request.title())
                .description(request.description())
                .language(request.language() == null ? "en" : request.language())
                .pageCount(request.pageCount())
                .publicationYear(request.publicationYear())
                .price(request.price())
                .stockQuantity(request.stockQuantity() == null ? 0 : request.stockQuantity())
                .isActive(request.isActive() == null ? true : request.isActive())
                .build();

        if (request.publisherId() != null) {
            book.setPublisher(publisherRepository.findById(request.publisherId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Publisher not found with id: " + request.publisherId())));
        }
        if (request.categoryId() != null) {
            book.setCategory(categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + request.categoryId())));
        }
        Set<Long> authorIds = new HashSet<>(
                request.authorIds() == null ? List.of() : request.authorIds());
        if (!authorIds.isEmpty()) {
            List<Author> authors = authorRepository.findAllById(authorIds);
            if (authors.size() != authorIds.size()) {
                throw new ResourceNotFoundException("One or more authors were not found");
            }
            book.setAuthors(new HashSet<>(authors));
        }

        return bookMapper.toResponse(bookRepository.saveAndFlush(book));
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id));
        book.setIsActive(false);
        bookRepository.save(book);
    }

    /**
     * Lists active books, paginated and sorted by title.
     */
    public Page<BookResponse> listActiveBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        return bookRepository.findAllByIsActiveTrue(pageable)
                .map(bookMapper::toResponse);
    }

    /**
     * Returns a single book by id, including its authors.
     */
    public BookResponse getBook(Long id) {
        return bookRepository.findWithDetailsById(id)
                .map(bookMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id));
    }
}
