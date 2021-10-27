package com.mprog.bootandjdbc.repository;

import com.mprog.bootandjdbc.domain.Author;

import java.util.Collection;

public interface AuthorRepository {
    Collection<Author> findAllWithoutBooksByNamePart(String namePart);
}
