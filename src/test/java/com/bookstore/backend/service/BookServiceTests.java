package com.bookstore.backend.service;

import com.bookstore.backend.dto.BookDtos.CreateBookRequest;
import com.bookstore.backend.dto.BookMapper;
import com.bookstore.backend.entity.Author;
import com.bookstore.backend.entity.Book;
import com.bookstore.backend.entity.Category;
import com.bookstore.backend.entity.Publisher;
import com.bookstore.backend.exception.ResourceNotFoundException;
import com.bookstore.backend.repository.AuthorRepository;
import com.bookstore.backend.repository.BookRepository;
import com.bookstore.backend.repository.CategoryRepository;
import com.bookstore.backend.repository.PublisherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTests {

    @Mock private BookRepository books;
    @Mock private AuthorRepository authors;
    @Mock private PublisherRepository publishers;
    @Mock private CategoryRepository categories;

    private BookService service;

    @BeforeEach
    void setUp() {
        service = new BookService(books, new BookMapper(), authors, publishers, categories);
    }

    @Test
    void createsBookWithDefaults() {
        when(books.saveAndFlush(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(42L);
            return book;
        });

        var response = service.createBook(request(null, null, null));

        assertEquals(42L, response.id());
        assertEquals("isbn", response.isbn());
        assertEquals("Title", response.title());
        assertEquals(new BigDecimal("12.50"), response.price());
        assertEquals("en", response.language());
        assertEquals(0, response.stockQuantity());
        assertTrue(response.isActive());
        assertTrue(response.authors().isEmpty());
        assertNull(response.publisher());
        assertNull(response.category());
        verifyNoInteractions(authors, publishers, categories);
    }

    @Test
    void createsBookWithExistingRelationshipsAndExplicitValues() {
        when(publishers.findById(1L)).thenReturn(Optional.of(
                Publisher.builder().id(1L).name("Publisher").build()));
        when(categories.findById(2L)).thenReturn(Optional.of(
                Category.builder().id(2L).name("Category").build()));
        when(authors.findAllById(Set.of(3L))).thenReturn(List.of(
                Author.builder().id(3L).firstName("First").lastName("Last").build()));
        when(books.saveAndFlush(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var request = new CreateBookRequest("isbn", "Title", "Description", "es",
                100, (short) 2026, new BigDecimal("12.50"), 5, false, 1L, 2L, List.of(3L, 3L));

        var response = service.createBook(request);

        assertEquals("Description", response.description());
        assertEquals("es", response.language());
        assertEquals(100, response.pageCount());
        assertEquals((short) 2026, response.publicationYear());
        assertEquals(5, response.stockQuantity());
        assertFalse(response.isActive());
        assertEquals(1L, response.publisher().id());
        assertEquals(2L, response.category().id());
        assertEquals(1, response.authors().size());
        assertEquals(3L, response.authors().get(0).id());
    }

    @Test
    void rejectsDuplicateIsbn() {
        when(books.findByIsbn("isbn")).thenReturn(Optional.of(new Book()));

        assertThrows(DataIntegrityViolationException.class,
                () -> service.createBook(request(null, null, null)));

        verify(books, never()).saveAndFlush(any());
    }

    @Test
    void rejectsMissingPublisher() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.createBook(request(1L, null, null)));
        verify(books, never()).saveAndFlush(any());
    }

    @Test
    void rejectsMissingCategory() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.createBook(request(null, 2L, null)));
        verify(books, never()).saveAndFlush(any());
    }

    @Test
    void rejectsMissingAuthors() {
        when(authors.findAllById(Set.of(3L, 4L))).thenReturn(List.of(
                Author.builder().id(3L).build()));

        assertThrows(ResourceNotFoundException.class,
                () -> service.createBook(request(null, null, List.of(3L, 4L))));
        verify(books, never()).saveAndFlush(any());
    }

    @Test
    void softDeletesWithoutRemovingBookOrRelationships() {
        Author author = Author.builder().id(3L).build();
        Book book = Book.builder().id(42L).isActive(true).authors(Set.of(author)).build();
        when(books.findById(42L)).thenReturn(Optional.of(book));

        service.deleteBook(42L);

        assertFalse(book.getIsActive());
        assertEquals(Set.of(author), book.getAuthors());
        verify(books).save(book);
        verify(books, never()).delete(any());
        verify(books, never()).deleteById(any());
    }

    @Test
    void deletingInactiveBookIsIdempotent() {
        Book book = Book.builder().id(42L).isActive(false).build();
        when(books.findById(42L)).thenReturn(Optional.of(book));

        service.deleteBook(42L);

        assertFalse(book.getIsActive());
    }

    @Test
    void rejectsDeletingUnknownBook() {
        assertThrows(ResourceNotFoundException.class, () -> service.deleteBook(42L));
        verify(books, never()).save(any());
    }

    @Test
    void getsBookWithDetails() {
        when(books.findWithDetailsById(42L)).thenReturn(Optional.of(
                Book.builder().id(42L).title("Title").isActive(false).build()));

        var response = service.getBook(42L);

        assertEquals(42L, response.id());
        assertEquals("Title", response.title());
        assertFalse(response.isActive());
    }

    @Test
    void rejectsGettingUnknownBook() {
        assertThrows(ResourceNotFoundException.class, () -> service.getBook(42L));
    }

    @Test
    void listsActiveBooksWithRequestedPaginationAndTitleSort() {
        var pageable = PageRequest.of(1, 5, Sort.by("title").ascending());
        when(books.findAllByIsActiveTrue(pageable)).thenReturn(new PageImpl<>(
                List.of(Book.builder().id(42L).title("Title").build()), pageable, 6));

        var response = service.listActiveBooks(1, 5);

        assertEquals(6, response.getTotalElements());
        assertEquals(1, response.getNumber());
        assertEquals(5, response.getSize());
        assertEquals("Title", response.getContent().get(0).title());
        verify(books).findAllByIsActiveTrue(pageable);
    }

    @Test
    void rejectsInvalidPaginationBeforeQueryingRepository() {
        assertThrows(IllegalArgumentException.class, () -> service.listActiveBooks(-1, 20));
        assertThrows(IllegalArgumentException.class, () -> service.listActiveBooks(0, 0));
        verifyNoInteractions(books);
    }

    private CreateBookRequest request(Long publisherId, Long categoryId, List<Long> authorIds) {
        return new CreateBookRequest("isbn", "Title", null, null, null, null,
                new BigDecimal("12.50"), null, null, publisherId, categoryId, authorIds);
    }
}
