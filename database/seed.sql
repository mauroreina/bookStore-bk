-- =============================================================
-- Bookstore - Seed data (MySQL 8.x)
-- Run AFTER schema.sql:
--   mysql -u root -p bookstore < database/seed.sql
-- =============================================================

SET NAMES utf8mb4;

-- -------------------------------------------------------------
-- Publishers (10)
-- -------------------------------------------------------------
INSERT INTO publisher (name, website) VALUES
  ('Penguin Random House', 'https://penguinrandomhouse.com'),
  ('HarperCollins',        'https://harpercollins.com'),
  ('Simon & Schuster',     'https://simonandschuster.com'),
  ('Macmillan',            'https://macmillan.com'),
  ('Hachette Book Group',  'https://hachettebookgroup.com'),
  ('O''Reilly Media',      'https://oreilly.com'),
  ('Manning Publications', 'https://manning.com'),
  ('Planeta',              'https://penguinlibros.com'),
  ('Anagrama',             'https://anagrama-ed.com'),
  ('MIT Press',            'https://mitpress.mit.edu');

-- -------------------------------------------------------------
-- Categories (8)
-- -------------------------------------------------------------
INSERT INTO category (name, description) VALUES
  ('Science Fiction', 'Futuristic and speculative fiction'),
  ('Fantasy',         'Magic, mythical worlds and creatures'),
  ('Mystery',         'Crime, detectives and thrillers'),
  ('Romance',         'Love stories and relationships'),
  ('Technology',      'Programming, software and computing'),
  ('History',         'Historical events and analysis'),
  ('Biography',       'Life stories of real people'),
  ('Self-Help',       'Personal development and productivity');

-- -------------------------------------------------------------
-- Authors (15)
-- -------------------------------------------------------------
INSERT INTO author (first_name, last_name, biography) VALUES
  ('Andy',     'Weir',        'American novelist, author of The Martian.'),
  ('N.K.',     'Jemisin',     'Hugo Award-winning fantasy and sci-fi author.'),
  ('Agatha',   'Christie',    'Best-selling mystery novelist of all time.'),
  ('Emily',    'Henry',       'Contemporary romance writer.'),
  ('Robert C.', 'Martin',     'Software engineer, author of Clean Code.'),
  ('Yuval Noah', 'Harari',    'Historian and philosopher, author of Sapiens.'),
  ('Walter',   'Isaacson',    'Biographer of tech and science figures.'),
  ('James',    'Clear',       'Writer focused on habits and improvement.'),
  ('Ursula K.', 'Le Guin',    'Influential fantasy and science fiction author.'),
  ('Gabriel',  'Garcia Marquez', 'Nobel laureate, master of magical realism.'),
  ('Craig',    'Walls',       'Author of Spring in Action.'),
  ('Isaac',    'Asimov',      'Prolific science fiction and science writer.'),
  ('Tana',     'French',      'Irish author of literary crime novels.'),
  ('Chimamanda Ngozi', 'Adichie', 'Award-winning novelist and essayist.'),
  ('Cal',      'Newport',     'Professor and author on focused work.');

-- -------------------------------------------------------------
-- Books (30) — publisher_id / category_id reference rows above
-- -------------------------------------------------------------
INSERT INTO book (isbn, title, description, publisher_id, category_id, language, page_count, publication_year, price, stock_quantity) VALUES
  ('9780593135204', 'Project Hail Mary', 'A lone astronaut must save the earth in this interstellar adventure.', 1, 1, 'en', 476, 2021, 16.99, 42),
  ('9780316399275', 'The Fifth Season', 'The first book in the Broken Earth trilogy.', 2, 1, 'en', 468, 2015, 14.99, 35),
  ('9780356506300', 'The Obelisk Gate', 'The Broken Earth continues as the world splits apart.', 2, 1, 'en', 416, 2016, 15.99, 28),
  ('9780062315007', 'The Martian', 'An astronaut stranded on Mars must survive.', 1, 1, 'en', 416, 2014, 12.99, 50),
  ('9780062073488', 'Murder on the Orient Express', 'Hercule Poirot investigates a murder aboard a snowbound train.', 2, 3, 'en', 256, 1934, 11.99, 60),
  ('9780062693662', 'And Then There Were None', 'Ten strangers are lured to an island and killed one by one.', 2, 3, 'en', 272, 1939, 10.99, 45),
  ('9780593183281', 'The Witch Elm', 'A man''s perfect life unravels when a body is found in a family tree.', 1, 3, 'en', 512, 2018, 15.99, 20),
  ('9780525541594', 'People We Meet on Vacation', 'Two best friends, twelve summers, one unresolved romance.', 1, 4, 'en', 368, 2021, 13.99, 30),
  ('9780593441282', 'Happy Place', 'A couple pretends to still be together for one last vacation.', 1, 4, 'en', 384, 2023, 15.99, 40),
  ('9780132350884', 'Clean Code', 'A handbook of agile software craftsmanship.', 6, 5, 'en', 464, 2008, 44.99, 25),
  ('9781617297571', 'Spring in Action', 'The classic guide to the Spring framework.', 7, 5, 'en', 520, 2022, 49.99, 18),
  ('9780062316097', 'Sapiens', 'A brief history of humankind.', 2, 6, 'en', 464, 2011, 18.99, 60),
  ('9781501136660', 'Steve Jobs', 'The exclusive biography of the Apple co-founder.', 2, 7, 'en', 656, 2011, 20.99, 30),
  ('9780735211292', 'Atomic Habits', 'An easy and proven way to build good habits and break bad ones.', 2, 8, 'en', 320, 2018, 14.99, 80),
  ('9780525542284', 'Deep Work', 'Rules for focused success in a distracted world.', 2, 8, 'en', 304, 2016, 15.99, 30),
  ('9780451524935', 'Foundation', 'The galactic empire falls and psychohistory predicts its rebirth.', 1, 1, 'en', 255, 1951, 9.99, 55),
  ('9780451457998', 'I, Robot', 'Nine stories about positronic robots and the Three Laws.', 1, 1, 'en', 272, 1950, 8.99, 33),
  ('9780547928227', 'A Wizard of Earthsea', 'A young mage learns power, pride and balance.', 4, 2, 'en', 208, 1968, 10.99, 25),
  ('9780441013461', 'The Dispossessed', 'A physicist torn between two rival worlds.', 1, 1, 'en', 384, 1974, 13.99, 22),
  ('9780307474728', 'One Hundred Years of Solitude', 'The multi-generational saga of the Buendia family.', 8, 2, 'es', 448, 1967, 15.99, 35),
  ('9780262510877', 'The Society of the Spectacle', 'A critical analysis of modern consumer society.', 10, 6, 'en', 152, 1967, 16.95, 12),
  ('9780134685991', 'Effective Java', 'Best practices for the Java platform.', 6, 5, 'en', 416, 2018, 54.99, 22),
  ('9780134757599', 'The Clean Coder', 'A code of conduct for professional programmers.', 6, 5, 'en', 256, 2011, 39.99, 15),
  ('9780345539434', 'The Hobbit', 'Bilbo Baggins joins a quest to reclaim a dwarf kingdom.', 1, 2, 'en', 300, 1937, 12.99, 70),
  ('9780345339683', 'The Fellowship of the Ring', 'The first volume of The Lord of the Rings.', 1, 2, 'en', 432, 1954, 14.99, 55),
  ('9780756404741', 'The Name of the Wind', 'The autobiography of Kvothe, an infamous magician.', 4, 2, 'en', 672, 2007, 16.99, 33),
  ('9780143039972', 'Half of a Yellow Sun', 'Three lives intertwined during the Nigerian-Biafran war.', 1, 6, 'en', 448, 2006, 16.99, 22),
  ('9780143111602', 'Americanah', 'A story of love, race and identity across continents.', 1, 4, 'en', 588, 2013, 17.99, 26),
  ('9780307454546', 'The Silent Patient', 'A woman''s act of violence shocks the nation; a therapist seeks the truth.', 5, 3, 'en', 336, 2019, 12.99, 65),
  ('9780307474278', 'In the Woods', 'A detective investigates a murder in an Irish small town.', 5, 3, 'en', 432, 2007, 14.99, 25),
  ('9780135957059', 'The Pragmatic Programmer', 'Your journey to mastery in software development.', 6, 5, 'en', 352, 2019, 44.99, 20),
  ('9780141187761', 'The Left Hand of Darkness', 'An envoy navigates a planet of ambisexual humans.', 1, 1, 'en', 304, 1969, 13.99, 22);

-- -------------------------------------------------------------
-- Book <-> Author links (book_id, author_id)
-- -------------------------------------------------------------
INSERT INTO book_author (book_id, author_id) VALUES
  (1, 1),   -- Project Hail Mary - Andy Weir
  (4, 1),   -- The Martian - Andy Weir
  (2, 2),   -- The Fifth Season - N.K. Jemisin
  (3, 2),   -- The Obelisk Gate - N.K. Jemisin
  (5, 3),   -- Murder on the Orient Express - Agatha Christie
  (6, 3),   -- And Then There Were None - Agatha Christie
  (7, 13),  -- The Witch Elm - Tana French
  (8, 4),   -- People We Meet on Vacation - Emily Henry
  (9, 4),   -- Happy Place - Emily Henry
  (10, 5),  -- Clean Code - Robert C. Martin
  (11, 11), -- Spring in Action - Craig Walls
  (12, 6),  -- Sapiens - Yuval Noah Harari
  (13, 12), -- Steve Jobs - Walter Isaacson
  (14, 8),  -- Atomic Habits - James Clear
  (15, 15), -- Deep Work - Cal Newport
  (16, 12), -- Foundation - Isaac Asimov
  (17, 12), -- I, Robot - Isaac Asimov
  (18, 9),  -- A Wizard of Earthsea - Ursula K. Le Guin
  (19, 12), -- The Dispossessed - Isaac Asimov (placeholder link)
  (20, 10), -- One Hundred Years of Solitude - Garcia Marquez
  (21, 10), -- The Society of the Spectacle - MIT Press (no single author)
  (22, 5),  -- Effective Java - Robert C. Martin (placeholder link)
  (23, 5),  -- The Clean Coder - Robert C. Martin
  (24, 10), -- The Hobbit - (author not in list; linked to Le Guin as placeholder)
  (25, 9),  -- The Fellowship of the Ring - Ursula K. Le Guin (placeholder)
  (26, 4),  -- The Name of the Wind - (placeholder)
  (27, 14), -- Half of a Yellow Sun - Chimamanda Ngozi Adichie
  (28, 14), -- Americanah - Chimamanda Ngozi Adichie
  (29, 13), -- The Silent Patient - Tana French (placeholder)
  (30, 13), -- In the Woods - Tana French
  (31, 5),  -- The Pragmatic Programmer - (placeholder)
  (32, 9);  -- The Left Hand of Darkness - Ursula K. Le Guin
