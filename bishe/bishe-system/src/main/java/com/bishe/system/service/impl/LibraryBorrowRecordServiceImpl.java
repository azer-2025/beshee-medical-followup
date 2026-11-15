package com.bishe.system.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bishe.common.core.domain.entity.SysUser;
import com.bishe.common.exception.ServiceException;
import com.bishe.common.utils.DateUtils;
import com.bishe.common.utils.StringUtils;
import com.bishe.common.utils.uuid.IdUtils;
import com.bishe.system.domain.LibraryBook;
import com.bishe.system.domain.LibraryBorrowRecord;
import com.bishe.system.domain.LibraryReader;
import com.bishe.system.mapper.LibraryBookMapper;
import com.bishe.system.mapper.LibraryBorrowRecordMapper;
import com.bishe.system.mapper.LibraryReaderMapper;
import com.bishe.system.service.ILibraryBorrowRecordService;
import com.bishe.system.service.ILibraryReaderService;
import com.bishe.system.service.ISysUserService;

@Service
public class LibraryBorrowRecordServiceImpl implements ILibraryBorrowRecordService
{
    private static final int MAX_BORROW_DAYS = 90;
    private static final int MAX_RENEW_COUNT = 9;
    private static final int RENEW_DAYS = 15;

    @Autowired
    private LibraryBorrowRecordMapper libraryBorrowRecordMapper;

    @Autowired
    private LibraryBookMapper libraryBookMapper;

    @Autowired
    private LibraryReaderMapper libraryReaderMapper;

    @Autowired
    private ILibraryReaderService libraryReaderService;

    @Autowired
    private ISysUserService userService;

    @Override
    public List<LibraryBorrowRecord> selectLibraryBorrowRecordList(LibraryBorrowRecord record)
    {
        libraryBorrowRecordMapper.updateOverdueBorrowRecordStatus();
        return libraryBorrowRecordMapper.selectLibraryBorrowRecordList(record);
    }

    @Override
    public List<LibraryBorrowRecord> selectMyBorrowRecordList(Long userId)
    {
        libraryBorrowRecordMapper.updateOverdueBorrowRecordStatus();
        return libraryBorrowRecordMapper.selectMyBorrowRecordList(userId);
    }

    @Override
    public LibraryBorrowRecord selectLibraryBorrowRecordById(Long recordId)
    {
        libraryBorrowRecordMapper.updateOverdueBorrowRecordStatus();
        return libraryBorrowRecordMapper.selectLibraryBorrowRecordById(recordId);
    }

    @Override
    @Transactional
    public int insertLibraryBorrowRecord(LibraryBorrowRecord record)
    {
        validateBorrow(record.getReaderId(), record.getBookId());

        Date borrowDate = StringUtils.isNotNull(record.getBorrowDate()) ? record.getBorrowDate() : DateUtils.getNowDate();
        int borrowDays = StringUtils.isNull(record.getBorrowDays()) || record.getBorrowDays() <= 0 ? 30 : record.getBorrowDays();
        if (borrowDays > MAX_BORROW_DAYS)
        {
            throw new ServiceException("借阅天数不能超过 " + MAX_BORROW_DAYS + " 天");
        }
        Date dueDate = StringUtils.isNotNull(record.getDueDate()) ? record.getDueDate() : DateUtils.addDays(borrowDate, borrowDays);
        if (dueDate.before(borrowDate))
        {
            throw new ServiceException("应还时间不能早于借阅时间");
        }

        record.setBorrowNo(buildBorrowNo());
        record.setBorrowDate(borrowDate);
        record.setDueDate(dueDate);
        record.setStatus("0");
        record.setRenewCount(buildRenewCount(record.getRenewCount()));

        int rows = libraryBorrowRecordMapper.insertLibraryBorrowRecord(record);
        if (rows > 0)
        {
            if (libraryBookMapper.decreaseAvailableStock(record.getBookId()) <= 0)
            {
                throw new ServiceException("图书库存不足");
            }
            libraryBookMapper.increaseBorrowStats(record.getBookId());
            libraryReaderMapper.increaseBorrowStats(record.getReaderId());
        }
        return rows;
    }

    @Override
    public int updateLibraryBorrowRecord(LibraryBorrowRecord record)
    {
        LibraryBorrowRecord oldRecord = libraryBorrowRecordMapper.selectLibraryBorrowRecordById(record.getRecordId());
        if (StringUtils.isNull(oldRecord))
        {
            throw new ServiceException("借阅记录不存在");
        }
        if ("1".equals(oldRecord.getStatus()))
        {
            throw new ServiceException("已归还记录不允许修改");
        }

        Date dueDate = StringUtils.isNotNull(record.getDueDate()) ? record.getDueDate() : oldRecord.getDueDate();
        if (StringUtils.isNull(dueDate))
        {
            throw new ServiceException("应还时间不能为空");
        }
        if (dueDate.before(oldRecord.getBorrowDate()))
        {
            throw new ServiceException("应还时间不能早于借阅时间");
        }

        Integer renewCount = StringUtils.isNull(record.getRenewCount()) ? oldRecord.getRenewCount() : record.getRenewCount();
        renewCount = buildRenewCount(renewCount);
        if (renewCount < oldRecord.getRenewCount())
        {
            throw new ServiceException("续借次数不能小于原有值");
        }

        record.setDueDate(dueDate);
        record.setRenewCount(renewCount);
        record.setStatus(resolveBorrowStatus(dueDate));
        record.setReturnDate(null);
        return libraryBorrowRecordMapper.updateLibraryBorrowRecord(record);
    }

    @Override
    @Transactional
    public int returnLibraryBorrowRecord(Long recordId, String updateBy)
    {
        LibraryBorrowRecord oldRecord = libraryBorrowRecordMapper.selectLibraryBorrowRecordById(recordId);
        if (StringUtils.isNull(oldRecord))
        {
            throw new ServiceException("借阅记录不存在");
        }
        if ("1".equals(oldRecord.getStatus()))
        {
            throw new ServiceException("该记录已归还");
        }

        LibraryBorrowRecord record = new LibraryBorrowRecord();
        record.setRecordId(recordId);
        record.setUpdateBy(updateBy);
        record.setReturnDate(DateUtils.getNowDate());

        int rows = libraryBorrowRecordMapper.returnLibraryBorrowRecord(record);
        if (rows > 0)
        {
            libraryBookMapper.increaseAvailableStock(oldRecord.getBookId());
            libraryReaderMapper.decreaseCurrentBorrowCount(oldRecord.getReaderId());
        }
        return rows;
    }

    @Override
    @Transactional
    public int renewLibraryBorrowRecord(Long recordId, String updateBy)
    {
        LibraryBorrowRecord oldRecord = libraryBorrowRecordMapper.selectLibraryBorrowRecordById(recordId);
        if (StringUtils.isNull(oldRecord))
        {
            throw new ServiceException("借阅记录不存在");
        }
        if ("1".equals(oldRecord.getStatus()))
        {
            throw new ServiceException("已归还记录不能续借");
        }

        LibraryBorrowRecord record = buildRenewRecord(oldRecord, updateBy);
        return libraryBorrowRecordMapper.renewLibraryBorrowRecord(record);
    }

    @Override
    @Transactional
    public int remindLibraryBorrowRecord(Long recordId, String updateBy)
    {
        LibraryBorrowRecord oldRecord = libraryBorrowRecordMapper.selectLibraryBorrowRecordById(recordId);
        if (StringUtils.isNull(oldRecord))
        {
            throw new ServiceException("借阅记录不存在");
        }
        if ("1".equals(oldRecord.getStatus()))
        {
            throw new ServiceException("已归还记录无需催还");
        }
        if (oldRecord.getDueDate().after(DateUtils.getNowDate()))
        {
            throw new ServiceException("当前记录尚未逾期，无需催还");
        }

        LibraryBorrowRecord record = new LibraryBorrowRecord();
        record.setRecordId(recordId);
        record.setLastReminderTime(DateUtils.getNowDate());
        record.setUpdateBy(updateBy);
        return libraryBorrowRecordMapper.remindLibraryBorrowRecord(record);
    }

    @Override
    @Transactional
    public int deleteLibraryBorrowRecordByIds(Long[] recordIds)
    {
        for (Long recordId : recordIds)
        {
            LibraryBorrowRecord record = libraryBorrowRecordMapper.selectLibraryBorrowRecordById(recordId);
            if (StringUtils.isNotNull(record) && !"1".equals(record.getStatus()))
            {
                throw new ServiceException("存在未归还记录，不能删除");
            }
        }
        return libraryBorrowRecordMapper.deleteLibraryBorrowRecordByIds(recordIds);
    }

    @Override
    @Transactional
    public int borrowBookByUser(Long userId, Long bookId)
    {
        LibraryReader reader = libraryReaderService.ensureLibraryReaderByUserId(userId);
        if (StringUtils.isNull(reader))
        {
            throw new ServiceException("当前账号未绑定读者信息，请联系管理员开通");
        }
        SysUser user = userService.selectUserById(userId);

        LibraryBorrowRecord record = new LibraryBorrowRecord();
        record.setReaderId(reader.getReaderId());
        record.setBookId(bookId);
        record.setBorrowDays(30);
        record.setCreateBy(StringUtils.isNotNull(user) ? user.getUserName() : "system");
        return insertLibraryBorrowRecord(record);
    }

    @Override
    @Transactional
    public int returnBookByUser(Long userId, Long recordId)
    {
        LibraryReader reader = libraryReaderService.ensureLibraryReaderByUserId(userId);
        if (StringUtils.isNull(reader))
        {
            throw new ServiceException("当前账号未绑定读者信息");
        }
        LibraryBorrowRecord record = libraryBorrowRecordMapper.selectLibraryBorrowRecordById(recordId);
        if (StringUtils.isNull(record) || !reader.getReaderId().equals(record.getReaderId()))
        {
            throw new ServiceException("无权归还该借阅记录");
        }
        SysUser user = userService.selectUserById(userId);
        return returnLibraryBorrowRecord(recordId, StringUtils.isNotNull(user) ? user.getUserName() : "system");
    }

    @Override
    @Transactional
    public int renewBookByUser(Long userId, Long recordId)
    {
        LibraryReader reader = libraryReaderService.ensureLibraryReaderByUserId(userId);
        if (StringUtils.isNull(reader))
        {
            throw new ServiceException("当前账号未绑定读者信息");
        }
        LibraryBorrowRecord oldRecord = libraryBorrowRecordMapper.selectLibraryBorrowRecordById(recordId);
        if (StringUtils.isNull(oldRecord) || !reader.getReaderId().equals(oldRecord.getReaderId()))
        {
            throw new ServiceException("无权续借该借阅记录");
        }
        if (!"0".equals(oldRecord.getStatus()))
        {
            throw new ServiceException("仅借阅中的记录可在线续借");
        }
        SysUser user = userService.selectUserById(userId);
        LibraryBorrowRecord record = buildRenewRecord(oldRecord, StringUtils.isNotNull(user) ? user.getUserName() : "system");
        return libraryBorrowRecordMapper.renewLibraryBorrowRecord(record);
    }

    @Override
    public Map<String, Object> selectMyBorrowSummary(Long userId)
    {
        libraryBorrowRecordMapper.updateOverdueBorrowRecordStatus();
        Map<String, Object> data = libraryBorrowRecordMapper.selectMyBorrowSummary(userId);
        return StringUtils.isNotNull(data) ? data : buildEmptySummary();
    }

    private void validateBorrow(Long readerId, Long bookId)
    {
        LibraryReader reader = libraryReaderMapper.selectLibraryReaderById(readerId);
        if (StringUtils.isNull(reader))
        {
            throw new ServiceException("读者不存在");
        }
        if (!"0".equals(reader.getStatus()))
        {
            throw new ServiceException("读者状态异常，不能借阅");
        }

        LibraryBook book = libraryBookMapper.selectLibraryBookById(bookId);
        if (StringUtils.isNull(book) || "2".equals(book.getDelFlag()))
        {
            throw new ServiceException("图书不存在");
        }
        if (!"0".equals(book.getStatus()))
        {
            throw new ServiceException("该图书已下架，不能借阅");
        }
        if (StringUtils.isNull(book.getAvailableStock()) || book.getAvailableStock() <= 0)
        {
            throw new ServiceException("该图书暂无可借库存");
        }

        int activeCount = libraryBorrowRecordMapper.countActiveBorrowByReaderId(readerId);
        if (activeCount >= reader.getBorrowLimit())
        {
            throw new ServiceException("已达到借阅上限");
        }
        if (libraryBorrowRecordMapper.countActiveBorrowByReaderAndBook(readerId, bookId) > 0)
        {
            throw new ServiceException("当前图书已在借阅中，请勿重复借阅");
        }
    }

    private LibraryBorrowRecord buildRenewRecord(LibraryBorrowRecord oldRecord, String updateBy)
    {
        int renewCount = buildRenewCount(oldRecord.getRenewCount() + 1);
        Date now = DateUtils.getNowDate();
        Date baseDate = oldRecord.getDueDate().after(now) ? oldRecord.getDueDate() : now;
        Date newDueDate = DateUtils.addDays(baseDate, RENEW_DAYS);

        LibraryBorrowRecord record = new LibraryBorrowRecord();
        record.setRecordId(oldRecord.getRecordId());
        record.setDueDate(newDueDate);
        record.setRenewCount(renewCount);
        record.setStatus("0");
        record.setUpdateBy(updateBy);
        return record;
    }

    private String buildBorrowNo()
    {
        return "BR" + DateUtils.dateTimeNow() + IdUtils.fastSimpleUUID().substring(0, 6).toUpperCase();
    }

    private Integer buildRenewCount(Integer renewCount)
    {
        int value = StringUtils.isNull(renewCount) ? 0 : renewCount;
        if (value < 0)
        {
            throw new ServiceException("续借次数不能小于 0");
        }
        if (value > MAX_RENEW_COUNT)
        {
            throw new ServiceException("续借次数不能超过 " + MAX_RENEW_COUNT + " 次");
        }
        return value;
    }

    private String resolveBorrowStatus(Date dueDate)
    {
        return dueDate.before(DateUtils.getNowDate()) ? "2" : "0";
    }

    private Map<String, Object> buildEmptySummary()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("totalCount", 0);
        map.put("borrowingCount", 0);
        map.put("returnedCount", 0);
        map.put("overdueCount", 0);
        map.put("pendingFineAmount", 0);
        return map;
    }
}
