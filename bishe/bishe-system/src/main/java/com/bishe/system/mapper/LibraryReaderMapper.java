package com.bishe.system.mapper;

import java.util.List;

import com.bishe.system.domain.LibraryReader;

public interface LibraryReaderMapper
{
    public List<LibraryReader> selectLibraryReaderList(LibraryReader reader);

    public LibraryReader selectLibraryReaderById(Long readerId);

    public LibraryReader selectLibraryReaderByUserId(Long userId);

    public LibraryReader selectLibraryReaderByReaderNo(String readerNo);

    public int insertLibraryReader(LibraryReader reader);

    public int updateLibraryReader(LibraryReader reader);

    public int updateLibraryReaderStatusByUserId(LibraryReader reader);

    public int deleteLibraryReaderByIds(Long[] readerIds);

    public int increaseBorrowStats(Long readerId);

    public int decreaseCurrentBorrowCount(Long readerId);
}
