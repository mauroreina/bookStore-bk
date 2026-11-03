package com.bookstore.backend.controller;

import com.bookstore.backend.dto.BookDtos.BookResponse;
import com.bookstore.backend.dto.BookDtos.CreateBookRequest;
import com.bookstore.backend.exception.GlobalExceptionHandler;
import com.bookstore.backend.exception.ResourceNotFoundException;
import com.bookstore.backend.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTests {

    @Mock private BookService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new BookController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsBookWithLocationAndResponse() throws Exception {
        when(service.createBook(any(CreateBookRequest.class))).thenReturn(book());

        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"isbn","title":"Title","price":12.50}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/books/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.isbn").value("isbn"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"isbn\":\" \",\"title\":\"Title\",\"price\":1}",
            "{\"isbn\":\"isbn\",\"title\":\" \",\"price\":1}",
            "{\"isbn\":\"123456789012345678901\",\"title\":\"Title\",\"price\":1}",
            "{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":-1}",
            "{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":1.001}",
            "{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":100000000}",
            "{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":1,\"language\":\" \"}",
            "{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":1,\"stockQuantity\":-1}",
            "{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":1,\"pageCount\":-1}",
            "{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":1,\"publicationYear\":-1}",
            "{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":1,\"publisherId\":0}",
            "{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":1,\"categoryId\":-1}",
            "{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":1,\"authorIds\":[null]}",
            "{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":1,\"authorIds\":[0]}"
    })
    void rejectsInvalidInput(String json) throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
        verifyNoInteractions(service);
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void returnsConflictForDuplicateIsbn() throws Exception {
        when(service.createBook(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":1}"))
                .andExpect(status().isConflict());
    }

    @Test
    void returnsNotFoundForMissingRelationship() throws Exception {
        when(service.createBook(any())).thenThrow(new ResourceNotFoundException("Author not found"));

        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"isbn\",\"title\":\"Title\",\"price\":1,\"authorIds\":[3]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletesBookWithNoContent() throws Exception {
        mvc.perform(delete("/api/books/42"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).deleteBook(42L);
    }

    @Test
    void rejectsInvalidBookId() throws Exception {
        mvc.perform(delete("/api/books/not-a-number")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void returnsNotFoundWhenDeletingUnknownBook() throws Exception {
        doThrow(new ResourceNotFoundException("Book not found")).when(service).deleteBook(42L);

        mvc.perform(delete("/api/books/42")).andExpect(status().isNotFound());
    }

    @Test
    void preservesGetRoutes() throws Exception {
        when(service.getBook(42L)).thenReturn(book());
        when(service.listActiveBooks(0, 20))
                .thenReturn(new PageImpl<>(List.of(book()), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/books/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42));
        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(42));
    }

    private BookResponse book() {
        return new BookResponse(42L, "isbn", "Title", null, "en", null, null,
                new BigDecimal("12.50"), 0, true, null, null, List.of());
    }
}
