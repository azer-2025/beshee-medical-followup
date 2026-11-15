package com.bishe.system.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.bishe.common.annotation.Excel;
import com.bishe.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 图书信息对象 lib_book
 */
public class LibraryBook extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long bookId;

    @Excel(name = "ISBN")
    private String isbn;

    @Excel(name = "图书名称")
    private String bookName;

    @Excel(name = "作者")
    private String author;

    @Excel(name = "出版社")
    private String publisher;

    @Excel(name = "分类")
    private String category;

    private String cover;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "出版日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date publishDate;

    @Excel(name = "价格")
    private BigDecimal price;

    @Excel(name = "馆藏总量")
    private Integer totalStock;

    @Excel(name = "可借库存")
    private Integer availableStock;

    @Excel(name = "累计借阅次数")
    private Integer borrowCount;

    @Excel(name = "馆藏位置")
    private String location;

    @Excel(name = "状态", readConverterExp = "0=正常,1=下架")
    private String status;

    @Excel(name = "图书简介")
    private String description;

    private String delFlag;

    public Long getBookId()
    {
        return bookId;
    }

    public void setBookId(Long bookId)
    {
        this.bookId = bookId;
    }

    public String getIsbn()
    {
        return isbn;
    }

    public void setIsbn(String isbn)
    {
        this.isbn = isbn;
    }

    public String getBookName()
    {
        return bookName;
    }

    public void setBookName(String bookName)
    {
        this.bookName = bookName;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getPublisher()
    {
        return publisher;
    }

    public void setPublisher(String publisher)
    {
        this.publisher = publisher;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getCover()
    {
        return cover;
    }

    public void setCover(String cover)
    {
        this.cover = cover;
    }

    public Date getPublishDate()
    {
        return publishDate;
    }

    public void setPublishDate(Date publishDate)
    {
        this.publishDate = publishDate;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public Integer getTotalStock()
    {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock)
    {
        this.totalStock = totalStock;
    }

    public Integer getAvailableStock()
    {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock)
    {
        this.availableStock = availableStock;
    }

    public Integer getBorrowCount()
    {
        return borrowCount;
    }

    public void setBorrowCount(Integer borrowCount)
    {
        this.borrowCount = borrowCount;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("bookId", getBookId())
            .append("isbn", getIsbn())
            .append("bookName", getBookName())
            .append("author", getAuthor())
            .append("publisher", getPublisher())
            .append("category", getCategory())
            .append("cover", getCover())
            .append("publishDate", getPublishDate())
            .append("price", getPrice())
            .append("totalStock", getTotalStock())
            .append("availableStock", getAvailableStock())
            .append("borrowCount", getBorrowCount())
            .append("location", getLocation())
            .append("status", getStatus())
            .append("description", getDescription())
            .append("remark", getRemark())
            .toString();
    }
}
