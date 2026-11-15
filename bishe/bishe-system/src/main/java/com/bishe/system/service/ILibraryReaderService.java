package com.bishe.system.service;

import java.util.List;

import com.bishe.system.domain.LibraryReader;

public interface ILibraryReaderService
{
    public List<LibraryReader> selectLibraryReaderList(LibraryReader reader);

    public LibraryReader selectLibraryReaderById(Long readerId);

    public LibraryReader selectLibraryReaderByUserId(Long userId);

    public LibraryReader ensureLibraryReaderByUserId(Long userId);

    public int insertLibraryReader(LibraryReader reader);

    public int updateLibraryReader(LibraryReader reader);

    public int deleteLibraryReaderByIds(Long[] readerIds);
}
