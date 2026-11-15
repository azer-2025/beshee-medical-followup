package com.bishe.system.domain;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.bishe.common.annotation.Excel;
import com.bishe.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 借阅记录对象 lib_borrow_record
 */
public class LibraryBorrowRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long recordId;

    @Excel(name = "借阅单号")
    private String borrowNo;

    private Long bookId;

    private Long readerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "借阅时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date borrowDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "应还时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "归还时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date returnDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近提醒时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastReminderTime;

    @Excel(name = "续借次数")
    private Integer renewCount;

    @Excel(name = "提醒次数")
    private Integer reminderCount;

    @Excel(name = "借阅状态", readConverterExp = "0=借阅中,1=已归还,2=已逾期")
    private String status;

    @Excel(name = "图书名称")
    private String bookName;

    @Excel(name = "ISBN")
    private String isbn;

    private String cover;

    @Excel(name = "作者")
    private String author;

    @Excel(name = "读者证号")
    private String readerNo;

    @Excel(name = "读者姓名")
    private String nickName;

    @Excel(name = "登录账号")
    private String userName;

    @Excel(name = "逾期天数")
    private Integer overdueDays;

    @Excel(name = "罚金")
    private BigDecimal fineAmount;

    private Integer borrowDays;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public String getBorrowNo()
    {
        return borrowNo;
    }

    public void setBorrowNo(String borrowNo)
    {
        this.borrowNo = borrowNo;
    }

    public Long getBookId()
    {
        return bookId;
    }

    public void setBookId(Long bookId)
    {
        this.bookId = bookId;
    }

    public Long getReaderId()
    {
        return readerId;
    }

    public void setReaderId(Long readerId)
    {
        this.readerId = readerId;
    }

    public Date getBorrowDate()
    {
        return borrowDate;
    }

    public void setBorrowDate(Date borrowDate)
    {
        this.borrowDate = borrowDate;
    }

    public Date getDueDate()
    {
        return dueDate;
    }

    public void setDueDate(Date dueDate)
    {
        this.dueDate = dueDate;
    }

    public Date getReturnDate()
    {
        return returnDate;
    }

    public void setReturnDate(Date returnDate)
    {
        this.returnDate = returnDate;
    }

    public Date getLastReminderTime()
    {
        return lastReminderTime;
    }

    public void setLastReminderTime(Date lastReminderTime)
    {
        this.lastReminderTime = lastReminderTime;
    }

    public Integer getRenewCount()
    {
        return renewCount;
    }

    public void setRenewCount(Integer renewCount)
    {
        this.renewCount = renewCount;
    }

    public Integer getReminderCount()
    {
        return reminderCount;
    }

    public void setReminderCount(Integer reminderCount)
    {
        this.reminderCount = reminderCount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getBookName()
    {
        return bookName;
    }

    public void setBookName(String bookName)
    {
        this.bookName = bookName;
    }

    public String getIsbn()
    {
        return isbn;
    }

    public void setIsbn(String isbn)
    {
        this.isbn = isbn;
    }

    public String getCover()
    {
        return cover;
    }

    public void setCover(String cover)
    {
        this.cover = cover;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getReaderNo()
    {
        return readerNo;
    }

    public void setReaderNo(String readerNo)
    {
        this.readerNo = readerNo;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public Integer getOverdueDays()
    {
        return overdueDays;
    }

    public void setOverdueDays(Integer overdueDays)
    {
        this.overdueDays = overdueDays;
    }

    public BigDecimal getFineAmount()
    {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount)
    {
        this.fineAmount = fineAmount;
    }

    public Integer getBorrowDays()
    {
        return borrowDays;
    }

    public void setBorrowDays(Integer borrowDays)
    {
        this.borrowDays = borrowDays;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("borrowNo", getBorrowNo())
            .append("bookId", getBookId())
            .append("readerId", getReaderId())
            .append("borrowDate", getBorrowDate())
            .append("dueDate", getDueDate())
            .append("returnDate", getReturnDate())
            .append("lastReminderTime", getLastReminderTime())
            .append("renewCount", getRenewCount())
            .append("reminderCount", getReminderCount())
            .append("status", getStatus())
            .append("overdueDays", getOverdueDays())
            .append("fineAmount", getFineAmount())
            .append("bookName", getBookName())
            .append("readerNo", getReaderNo())
            .append("nickName", getNickName())
            .append("remark", getRemark())
            .toString();
    }
}
