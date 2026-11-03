-- =============================================================
-- Bookstore - Initial schema (MySQL 8.x)
-- Run against an EMPTY database.
--   mysql -u root -p bookstore < database/schema.sql
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -------------------------------------------------------------
-- Catalog: authors, publishers, categories, books
-- -------------------------------------------------------------

CREATE TABLE author (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    first_name  VARCHAR(100)    NOT NULL,
    last_name   VARCHAR(100)    NOT NULL,
    biography   TEXT            NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE publisher (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name        VARCHAR(200)    NOT NULL,
    website     VARCHAR(255)    NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_publisher_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE category (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(500)    NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_category_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE book (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    isbn            VARCHAR(20)      NOT NULL,
    title           VARCHAR(255)     NOT NULL,
    description     TEXT             NULL,
    publisher_id    BIGINT UNSIGNED  NULL,
    category_id     BIGINT UNSIGNED  NULL,
    language        VARCHAR(50)      NOT NULL DEFAULT 'en',
    page_count      INT UNSIGNED     NULL,
    publication_year SMALLINT UNSIGNED NULL,
    price           DECIMAL(10, 2)   NOT NULL,
    stock_quantity  INT UNSIGNED     NOT NULL DEFAULT 0,
    is_active       BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_book_isbn (isbn),
    KEY idx_book_title (title),
    CONSTRAINT fk_book_publisher FOREIGN KEY (publisher_id) REFERENCES publisher (id) ON DELETE SET NULL,
    CONSTRAINT fk_book_category  FOREIGN KEY (category_id)  REFERENCES category (id)  ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- A book can have several authors; an author can write several books.
CREATE TABLE book_author (
    book_id   BIGINT UNSIGNED NOT NULL,
    author_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_ba_book   FOREIGN KEY (book_id)   REFERENCES book (id)   ON DELETE CASCADE,
    CONSTRAINT fk_ba_author FOREIGN KEY (author_id) REFERENCES author (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- -------------------------------------------------------------
-- Customers
-- -------------------------------------------------------------

CREATE TABLE customer (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255)    NOT NULL,
    password_hash VARCHAR(255)    NOT NULL,
    first_name    VARCHAR(100)    NOT NULL,
    last_name     VARCHAR(100)    NOT NULL,
    phone         VARCHAR(30)     NULL,
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_customer_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE address (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    customer_id  BIGINT UNSIGNED NOT NULL,
    street       VARCHAR(255)    NOT NULL,
    city         VARCHAR(100)    NOT NULL,
    state        VARCHAR(100)    NULL,
    postal_code  VARCHAR(20)     NOT NULL,
    country      VARCHAR(100)    NOT NULL,
    is_default   BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- -------------------------------------------------------------
-- Orders
-- -------------------------------------------------------------

CREATE TABLE orders (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    customer_id     BIGINT UNSIGNED  NOT NULL,
    address_id      BIGINT UNSIGNED  NULL,
    status          ENUM('PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    total_amount    DECIMAL(10, 2)   NOT NULL DEFAULT 0.00,
    created_at      TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_orders_customer (customer_id),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT fk_orders_address  FOREIGN KEY (address_id)  REFERENCES address (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE order_item (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id   BIGINT UNSIGNED NOT NULL,
    book_id    BIGINT UNSIGNED NOT NULL,
    quantity   INT UNSIGNED    NOT NULL,
    unit_price DECIMAL(10, 2)  NOT NULL COMMENT 'Price at purchase time',
    PRIMARY KEY (id),
    UNIQUE KEY uq_order_item (order_id, book_id),
    CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_oi_book  FOREIGN KEY (book_id)  REFERENCES book (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
