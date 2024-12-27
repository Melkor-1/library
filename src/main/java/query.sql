-- file to manually create tables and insert sample data

CREATE TABLE books (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    author TEXT NOT NULL,
    genre TEXT NOT NULL,
    total_count INTEGER,
    total_alloc INTEGER DEFAULT 0,
);

CREATE TABLE members (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    address TEXT NOT NULL,
    phone_no TEXT NOT NULL,
    alloc_book_id INTEGER REFERENCES books(id)
);

CREATE TABLE admin (
    id INTEGER PRIMARY KEY CHECK (id = 1), -- Ensure only one entry
    password_hash TEXT NOT NULL
);

INSERT INTO books (name, author, genre, total_count)
VALUES ('harry potter and the philosopher''s stone', 'j.k rowling', 'fiction', 5);

INSERT INTO books (name, author, genre, total_count)
VALUES ('harry potter and the chamber of secrets', 'j.k rowling', 'fiction', 5);

INSERT INTO books (name, author, genre, total_count)
VALUES ('harry potter and the prisoner of azkaban', 'j.k rowling', 'fiction', 5);

INSERT INTO books (name, author, genre, total_count)
VALUES ('harry potter and the goblet of fire', 'j.k rowling', 'fiction', 5);

INSERT INTO books (name, author, genre, total_count)
VALUES ('harry potter and the order of phoenix ', 'j.k rowling', 'fiction', 5);

INSERT INTO books (name, author, genre, total_count)
VALUES ('harry potter and the half-blood prince', 'j.k rowling', 'fiction', 5);

INSERT INTO books (name, author, genre, total_count)
VALUES ('harry potter and the deathly hallows', 'j.k rowling', 'fiction', 5);

INSERT INTO books (name, author, genre, total_count)
VALUES ('harry potter and the cursed child', 'j.k rowling', 'fiction', 5);

INSERT INTO books (name, author, genre, total_count)
VALUES ('brown', 'pink', 'orange', 2);

INSERT INTO books (name, author, genre, total_count)
VALUES ('brown', 'pink', 'yellow', 1);

INSERT INTO members (name, address, phone_no) VALUES ('haider', 'fb area', '1000');

INSERT INTO members (name, address, phone_no) VALUES ('ahsan', 'gulshan iqbal', '1001');

INSERT INTO members (name, address, phone_no) VALUES ('moin', 'serena market', '1002');
