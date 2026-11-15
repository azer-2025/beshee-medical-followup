package com.bishe.system.service;

import java.util.List;

import com.bishe.system.domain.LibraryBook;

public interface ILibraryBookService
{
    public List<LibraryBook> selectLibraryBookList(LibraryBook book);

    public List<LibraryBook> selectPortalBookList(LibraryBook book);

    public LibraryBook selectLibraryBookById(Long bookId);

    public int insertLibraryBook(LibraryBook book);

    public int updateLibraryBook(LibraryBook book);

    public int deleteLibraryBookByIds(Long[] bookIds);
}
