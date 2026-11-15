package com.bishe.system.mapper;

import java.util.List;

import com.bishe.system.domain.LibraryBook;

public interface LibraryBookMapper
{
    public List<LibraryBook> selectLibraryBookList(LibraryBook book);

    public List<LibraryBook> selectPortalBookList(LibraryBook book);

    public LibraryBook selectLibraryBookById(Long bookId);

    public LibraryBook selectLibraryBookByIsbn(String isbn);

    public int insertLibraryBook(LibraryBook book);

    public int updateLibraryBook(LibraryBook book);

    public int deleteLibraryBookByIds(Long[] bookIds);

    public int increaseBorrowStats(Long bookId);

    public int increaseAvailableStock(Long bookId);

    public int decreaseAvailableStock(Long bookId);
}
