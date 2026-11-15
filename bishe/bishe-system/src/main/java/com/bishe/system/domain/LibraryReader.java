package com.bishe.system.domain;

import com.bishe.common.annotation.Excel;
import com.bishe.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 读者信息对象 lib_reader
 */
public class LibraryReader extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long readerId;

    private Long userId;

    @Excel(name = "读者证号")
    private String readerNo;

    @Excel(name = "借书证号")
    private String cardNo;

    @Excel(name = "身份证号")
    private String idCard;

    @Excel(name = "借阅上限")
    private Integer borrowLimit;

    @Excel(name = "累计借阅次数")
    private Integer totalBorrowCount;

    @Excel(name = "当前借阅数")
    private Integer currentBorrowCount;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    @Excel(name = "登录账号")
    private String userName;

    @Excel(name = "读者姓名")
    private String nickName;

    @Excel(name = "手机号")
    private String phonenumber;

    @Excel(name = "邮箱")
    private String email;

    @Excel(name = "性别")
    private String sex;

    private String avatar;

    private String password;

    public Long getReaderId()
    {
        return readerId;
    }

    public void setReaderId(Long readerId)
    {
        this.readerId = readerId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getReaderNo()
    {
        return readerNo;
    }

    public void setReaderNo(String readerNo)
    {
        this.readerNo = readerNo;
    }

    public String getCardNo()
    {
        return cardNo;
    }

    public void setCardNo(String cardNo)
    {
        this.cardNo = cardNo;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public Integer getBorrowLimit()
    {
        return borrowLimit;
    }

    public void setBorrowLimit(Integer borrowLimit)
    {
        this.borrowLimit = borrowLimit;
    }

    public Integer getTotalBorrowCount()
    {
        return totalBorrowCount;
    }

    public void setTotalBorrowCount(Integer totalBorrowCount)
    {
        this.totalBorrowCount = totalBorrowCount;
    }

    public Integer getCurrentBorrowCount()
    {
        return currentBorrowCount;
    }

    public void setCurrentBorrowCount(Integer currentBorrowCount)
    {
        this.currentBorrowCount = currentBorrowCount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("readerId", getReaderId())
            .append("userId", getUserId())
            .append("readerNo", getReaderNo())
            .append("cardNo", getCardNo())
            .append("idCard", getIdCard())
            .append("borrowLimit", getBorrowLimit())
            .append("totalBorrowCount", getTotalBorrowCount())
            .append("currentBorrowCount", getCurrentBorrowCount())
            .append("status", getStatus())
            .append("userName", getUserName())
            .append("nickName", getNickName())
            .append("phonenumber", getPhonenumber())
            .append("email", getEmail())
            .append("sex", getSex())
            .append("remark", getRemark())
            .toString();
    }
}
