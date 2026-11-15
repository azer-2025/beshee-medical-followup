package com.bishe.system.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bishe.system.domain.LibraryBorrowRecord;

public interface LibraryBorrowRecordMapper
{
    public List<LibraryBorrowRecord> selectLibraryBorrowRecordList(LibraryBorrowRecord record);

    public List<LibraryBorrowRecord> selectMyBorrowRecordList(Long userId);

    public LibraryBorrowRecord selectLibraryBorrowRecordById(Long recordId);

    public int insertLibraryBorrowRecord(LibraryBorrowRecord record);

    public int updateLibraryBorrowRecord(LibraryBorrowRecord record);

    public int returnLibraryBorrowRecord(LibraryBorrowRecord record);

    public int renewLibraryBorrowRecord(LibraryBorrowRecord record);

    public int remindLibraryBorrowRecord(LibraryBorrowRecord record);

    public int deleteLibraryBorrowRecordByIds(Long[] recordIds);

    public int countActiveBorrowByReaderId(Long readerId);

    public int countActiveBorrowByReaderAndBook(@Param("readerId") Long readerId, @Param("bookId") Long bookId);

    public int countActiveBorrowByReaderIds(Long[] readerIds);

    public int countActiveBorrowByBookIds(Long[] bookIds);

    public int updateOverdueBorrowRecordStatus();

    public Map<String, Object> selectMyBorrowSummary(Long userId);
}
