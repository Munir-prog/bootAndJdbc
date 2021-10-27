package com.mprog.bootandjdbc.domain;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BookResultSetExtractor implements ResultSetExtractor<List<Book>> {

    @Override
    public List<Book> extractData(ResultSet rs) throws SQLException, DataAccessException {
        var books = new ArrayList<Book>();
        var authors = new HashMap<Long, Author>();
        var currentBook = new Book(0, null, 0, null);
        while (rs.next()) {
            var bookId = rs.getLong("b_id");
            if (currentBook.getId() != bookId) {
                currentBook = new Book(bookId, rs.getString("b_title"),
                        rs.getInt("b_publish_year"), new HashSet<>());
                books.add(currentBook);
            }

            var authorId = rs.getLong("a_id");
            if (authorId == 0)
                throw new RuntimeException("no author for book " + currentBook.getId());
            var author = authors.get(authorId);
            if (author == null) {
                var authorName = rs.getString("a_name");
                author = new Author(authorId, authorName, new HashSet<>());
                authors.put(authorId, author);
            }
            author.getBooks().add(currentBook);

            currentBook.getAuthors().add(author);
        }
        return books;
    }
}
