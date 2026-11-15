package com.bishe.system.service;

import java.util.List;
import java.util.Map;

import com.bishe.system.domain.LibraryBorrowRecord;

public interface ILibraryBorrowRecordService
{
    public List<LibraryBorrowRecord> selectLibraryBorrowRecordList(LibraryBorrowRecord record);

    public List<LibraryBorrowRecord> selectMyBorrowRecordList(Long userId);

    public LibraryBorrowRecord selectLibraryBorrowRecordById(Long recordId);

    public int insertLibraryBorrowRecord(LibraryBorrowRecord record);

    public int updateLibraryBorrowRecord(LibraryBorrowRecord record);

    public int returnLibraryBorrowRecord(Long recordId, String updateBy);

    public int renewLibraryBorrowRecord(Long recordId, String updateBy);

    public int remindLibraryBorrowRecord(Long recordId, String updateBy);

    public int deleteLibraryBorrowRecordByIds(Long[] recordIds);

    public int borrowBookByUser(Long userId, Long bookId);

    public int returnBookByUser(Long userId, Long recordId);

    public int renewBookByUser(Long userId, Long recordId);

    public Map<String, Object> selectMyBorrowSummary(Long userId);
}
