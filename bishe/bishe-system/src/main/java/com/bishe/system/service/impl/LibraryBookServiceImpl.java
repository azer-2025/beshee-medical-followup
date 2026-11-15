package com.bishe.system.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bishe.common.constant.UserConstants;
import com.bishe.common.exception.ServiceException;
import com.bishe.common.utils.StringUtils;
import com.bishe.system.domain.LibraryBook;
import com.bishe.system.mapper.LibraryBookMapper;
import com.bishe.system.mapper.LibraryBorrowRecordMapper;
import com.bishe.system.service.ILibraryBookService;

@Service
public class LibraryBookServiceImpl implements ILibraryBookService
{
    @Autowired
    private LibraryBookMapper libraryBookMapper;

    @Autowired
    private LibraryBorrowRecordMapper libraryBorrowRecordMapper;

    @Override
    public List<LibraryBook> selectLibraryBookList(LibraryBook book)
    {
        return libraryBookMapper.selectLibraryBookList(book);
    }

    @Override
    public List<LibraryBook> selectPortalBookList(LibraryBook book)
    {
        return libraryBookMapper.selectPortalBookList(book);
    }

    @Override
    public LibraryBook selectLibraryBookById(Long bookId)
    {
        return libraryBookMapper.selectLibraryBookById(bookId);
    }

    @Override
    public int insertLibraryBook(LibraryBook book)
    {
        validateBook(book);
        LibraryBook exists = libraryBookMapper.selectLibraryBookByIsbn(book.getIsbn());
        if (StringUtils.isNotNull(exists))
        {
            throw new ServiceException("新增图书失败，ISBN 已存在");
        }
        if (StringUtils.isNull(book.getTotalStock()) || book.getTotalStock() < 0)
        {
            throw new ServiceException("馆藏总量不能小于 0");
        }
        if (StringUtils.isEmpty(book.getStatus()))
        {
            book.setStatus(UserConstants.NORMAL);
        }
        book.setAvailableStock(book.getTotalStock());
        if (StringUtils.isNull(book.getBorrowCount()))
        {
            book.setBorrowCount(0);
        }
        return libraryBookMapper.insertLibraryBook(book);
    }

    @Override
    public int updateLibraryBook(LibraryBook book)
    {
        validateBook(book);
        LibraryBook oldBook = libraryBookMapper.selectLibraryBookById(book.getBookId());
        if (StringUtils.isNull(oldBook))
        {
            throw new ServiceException("图书信息不存在");
        }
        LibraryBook exists = libraryBookMapper.selectLibraryBookByIsbn(book.getIsbn());
        if (StringUtils.isNotNull(exists) && !exists.getBookId().equals(book.getBookId()))
        {
            throw new ServiceException("修改图书失败，ISBN 已存在");
        }

        int totalStock = StringUtils.isNull(book.getTotalStock()) ? 0 : book.getTotalStock();
        int oldTotalStock = StringUtils.isNull(oldBook.getTotalStock()) ? 0 : oldBook.getTotalStock();
        int oldAvailableStock = StringUtils.isNull(oldBook.getAvailableStock()) ? 0 : oldBook.getAvailableStock();
        int borrowedCount = Math.max(oldTotalStock - oldAvailableStock, 0);
        if (totalStock < borrowedCount)
        {
            throw new ServiceException("馆藏总量不能小于当前已借出数量");
        }
        book.setAvailableStock(totalStock - borrowedCount);
        return libraryBookMapper.updateLibraryBook(book);
    }

    @Override
    public int deleteLibraryBookByIds(Long[] bookIds)
    {
        if (libraryBorrowRecordMapper.countActiveBorrowByBookIds(bookIds) > 0)
        {
            throw new ServiceException("存在未归还借阅记录，不能删除图书");
        }
        return libraryBookMapper.deleteLibraryBookByIds(bookIds);
    }

    private void validateBook(LibraryBook book)
    {
        if (StringUtils.isEmpty(book.getIsbn()))
        {
            throw new ServiceException("ISBN 不能为空");
        }
        if (StringUtils.isEmpty(book.getBookName()))
        {
            throw new ServiceException("图书名称不能为空");
        }
        if (StringUtils.isEmpty(book.getAuthor()))
        {
            throw new ServiceException("作者不能为空");
        }
        if (StringUtils.isNotNull(book.getPrice()) && book.getPrice().signum() < 0)
        {
            throw new ServiceException("图书价格不能小于 0");
        }
    }
}
