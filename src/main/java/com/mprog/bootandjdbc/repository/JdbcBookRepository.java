package com.mprog.bootandjdbc.repository;

import com.mprog.bootandjdbc.domain.Author;
import com.mprog.bootandjdbc.domain.Book;
import com.mprog.bootandjdbc.domain.BookResultSetExtractor;
import com.mprog.bootandjdbc.domain.BookRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcOperations jdbc;

    @Override
    public Collection<Book> findAllWithoutAuthors() {
        String sql = """
                SELECT id, title, publish_year
                FROM books
                """;
        return jdbc.query(sql, new BookRowMapper());
    }

    @Override
    public Collection<Book> findAll() {
        String sql = """
                SELECT b.id b_id,
                    b.title b_title,
                    b.publish_year b_publish_year,
                    a.id a_id,
                    a.name a_name
                FROM books b LEFT JOIN books_authors ba ON b.id = ba.book_id
                    LEFT JOIN authors a ON ba.author_id = a.id
                ORDER BY b.id, a.id
                """;
        return jdbc.query(sql, new BookResultSetExtractor());
    }

    @Override
    public Collection<Book> findAllByTitlePart(String part) {
        String sql = """
                SELECT 
                    b.id b_id,
                    b.title b_title,
                    b.publish_year b_publish_year,
                    a.id a_id,
                    a.name a_name
                FROM books b LEFT JOIN books_authors ba ON b.id = ba.book_id
                    LEFT JOIN authors a ON ba.author_id = a.id
                WHERE lower(title) LIKE :titlePart
                ORDER BY b.id, a.id
                """;
        return jdbc.query(
                sql,
                Map.of("titlePart", "%" + part.strip().toLowerCase() + "%"),
                new BookResultSetExtractor()
        );
    }

    @Override
    public Optional<Book> findById(long id) {
        var list = jdbc.query("""
                        select
                            b.id b_id,
                            b.title b_title,
                            b.publish_year b_publish_year,
                            a.id a_id,
                            a.name a_name
                        from books b left join books_authors ba on b.id = ba.book_id
                            left join authors a on ba.author_id = a.id
                        where b.id = :id
                        """,
                Map.of("id", id),
                new BookResultSetExtractor());
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(0));
        }
    }

    @Override
    public void insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();
        var params = new MapSqlParameterSource();
        params.addValue("title", book.getTitle());
        params.addValue("publish_year", book.getYear());
        jdbc.update("""
                        insert into books (title, publish_year)
                        values (:title, :publish_year)
                        """,
                params,
                keyHolder);
        book.setId((Long) (keyHolder.getKeys().get("id")));
        insertAuthors(book);
    }

    @Override
    public void update(Book book) {
        var params = new MapSqlParameterSource();
        params.addValue("title", book.getTitle());
        params.addValue("publish_year", book.getYear());
        params.addValue("id", book.getId());
        jdbc.update("""
                        update books set title = :title, publish_year = :publish_year
                        where id = :id
                        """,
                params);
        deleteAuthors(book);
        insertAuthors(book);
    }

    private void insertAuthors(Book book) {
        if (book.getAuthors().size() > 0) {
            var paramArray = book.getAuthors().stream()
                    .map(author -> {
                        var params = new MapSqlParameterSource();
                        params.addValue("book_id", book.getId());
                        params.addValue("author_id", author.getId());
                        return params;
                    }).toArray(MapSqlParameterSource[]::new);
            jdbc.batchUpdate("insert into books_authors values(:book_id, :author_id)",
                    paramArray);
        }
    }

    private void deleteAuthors(Book book) {
        jdbc.update("delete from books_authors where book_id = :id",
                Map.of("id", book.getId()));
    }
}
