package com.bookstore.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class BookApiIT {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("bookstore_test")
            .withInitScript("database/schema.sql");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    }

    @Autowired private WebApplicationContext context;
    @Autowired private JdbcTemplate jdbc;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        jdbc.update("DELETE FROM order_item");
        jdbc.update("DELETE FROM orders");
        jdbc.update("DELETE FROM address");
        jdbc.update("DELETE FROM customer");
        jdbc.update("DELETE FROM book_author");
        jdbc.update("DELETE FROM book");
        jdbc.update("DELETE FROM author");
        jdbc.update("DELETE FROM publisher");
        jdbc.update("DELETE FROM category");
    }

    @Test
    void createsReadsAndSoftDeletesBookWhilePreservingOrderHistory() throws Exception {
        jdbc.update("INSERT INTO publisher (id, name) VALUES (1, 'Publisher')");
        jdbc.update("INSERT INTO category (id, name) VALUES (1, 'Category')");
        jdbc.update("INSERT INTO author (id, first_name, last_name) VALUES (1, 'First', 'Last')");

        String location = mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"9780134685991","title":"Effective Java","price":45.00,
                                 "publisherId":1,"categoryId":1,"authorIds":[1]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.stockQuantity").value(0))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.authors[0].firstName").value("First"))
                .andExpect(jsonPath("$.publisher.name").value("Publisher"))
                .andExpect(jsonPath("$.category.name").value("Category"))
                .andReturn().getResponse().getHeader("Location");
        assertNotNull(location);
        Long id = jdbc.queryForObject("SELECT id FROM book WHERE isbn = '9780134685991'", Long.class);

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authors[0].id").value(1))
                .andExpect(jsonPath("$.publisher.id").value(1))
                .andExpect(jsonPath("$.category.id").value(1));
        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        jdbc.update("""
                INSERT INTO customer (id, email, password_hash, first_name, last_name)
                VALUES (1, 'reader@example.com', 'test-only', 'Reader', 'Test')
                """);
        jdbc.update("INSERT INTO orders (id, customer_id) VALUES (1, 1)");
        jdbc.update("INSERT INTO order_item (order_id, book_id, quantity, unit_price) VALUES (1, ?, 1, 45.00)", id);

        mvc.perform(delete("/api/books/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/books/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
        assertFalse(jdbc.queryForObject("SELECT is_active FROM book WHERE id = ?", Boolean.class, id));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM book_author WHERE book_id = ?", Integer.class, id));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM order_item WHERE book_id = ?", Integer.class, id));
    }

    @Test
    void listsOnlyActiveBooksSortedAndPaginated() throws Exception {
        createBook("isbn-z", "Zulu");
        createBook("isbn-a", "Alpha");
        createBook("isbn-m", "Middle");
        jdbc.update("UPDATE book SET is_active = false WHERE isbn = 'isbn-m'");

        mvc.perform(get("/api/books").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Alpha"));
        mvc.perform(get("/api/books").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Zulu"));
    }

    @Test
    void rejectsDuplicateIsbnEvenAfterSoftDeletion() throws Exception {
        createBook("duplicate", "Original");
        Long id = jdbc.queryForObject("SELECT id FROM book WHERE isbn = 'duplicate'", Long.class);
        mvc.perform(delete("/api/books/{id}", id)).andExpect(status().isNoContent());

        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"duplicate\",\"title\":\"Duplicate\",\"price\":10}"))
                .andExpect(status().isConflict());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM book", Integer.class));
    }

    @Test
    void invalidRequestsDoNotPersistBooks() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"invalid\",\"title\":\"Title\",\"price\":-1}"))
                .andExpect(status().isBadRequest());
        for (String relationship : new String[] {
                "\"publisherId\":999", "\"categoryId\":999", "\"authorIds\":[999]"
        }) {
            mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"isbn\":\"missing\",\"title\":\"Title\",\"price\":10," + relationship + "}"))
                    .andExpect(status().isNotFound());
        }
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM book", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM book_author", Integer.class));
    }

    @Test
    void unknownBooksReturnNotFound() throws Exception {
        mvc.perform(get("/api/books/999")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/books/999")).andExpect(status().isNotFound());
    }

    private void createBook(String isbn, String title) throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"%s","title":"%s","price":10.00}
                                """.formatted(isbn, title)))
                .andExpect(status().isCreated());
    }
}
