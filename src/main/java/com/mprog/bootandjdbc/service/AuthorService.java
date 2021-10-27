package com.mprog.bootandjdbc.service;


import com.mprog.bootandjdbc.domain.Author;

import java.util.Collection;

public interface AuthorService {
    Collection<Author> findAllByNamePart(String namePart);
}
