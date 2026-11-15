package com.bishe.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.bishe.common.annotation.Excel;
import com.bishe.common.core.domain.BaseEntity;

/**
 * 健康预警对象 health_warning
 *
 * @author bishe
 * @date 2025-05-22
 */
public class HealthWarning extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 预警ID */
    private Long warningId;

    /** 患者ID */
    @Excel(name = "患者ID", isExport = false)
    private Long userId;

    /** 预警类型 */
    @Excel(name = "预警类型")
    private String warningType;

    /** 异常值 */
    @Excel(name = "异常值")
    private String warningValue;

    /** 预警等级 */
    @Excel(name = "预警等级", readConverterExp = "1=低,2=中,3=高")
    private String warningLevel;

    /** 预警消息 */
    @Excel(name = "预警消息")
    private String warningMsg;

    /** 阈值依据 */
    @Excel(name = "阈值依据")
    private String thresholdRange;

    /** 是否已读 */
    @Excel(name = "是否已读", readConverterExp = "0=否,1=是")
    private String isRead;

    /** 医生复核状态 */
    @Excel(name = "医生复核", readConverterExp = "0=待确认,1=已确认")
    private String confirmStatus;

    /** 复核说明 */
    @Excel(name = "复核说明")
    private String confirmNote;

    /** 复核医生 */
    @Excel(name = "复核医生")
    private String confirmBy;

    /** 复核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "复核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date confirmTime;

    /** 患者姓名 */
    @Excel(name = "患者姓名")
    private String patientName;

    public void setWarningId(Long warningId)
    {
        this.warningId = warningId;
    }

    public Long getWarningId()
    {
        return warningId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setWarningType(String warningType)
    {
        this.warningType = warningType;
    }

    public String getWarningType()
    {
        return warningType;
    }

    public void setWarningValue(String warningValue)
    {
        this.warningValue = warningValue;
    }

    public String getWarningValue()
    {
        return warningValue;
    }

    public void setWarningLevel(String warningLevel)
    {
        this.warningLevel = warningLevel;
    }

    public String getWarningLevel()
    {
        return warningLevel;
    }

    public void setWarningMsg(String warningMsg)
    {
        this.warningMsg = warningMsg;
    }

    public String getWarningMsg()
    {
        return warningMsg;
    }

    public void setThresholdRange(String thresholdRange)
    {
        this.thresholdRange = thresholdRange;
    }

    public String getThresholdRange()
    {
        return thresholdRange;
    }

    public void setIsRead(String isRead)
    {
        this.isRead = isRead;
    }

    public String getIsRead()
    {
        return isRead;
    }

    public void setConfirmStatus(String confirmStatus)
    {
        this.confirmStatus = confirmStatus;
    }

    public String getConfirmStatus()
    {
        return confirmStatus;
    }

    public void setConfirmNote(String confirmNote)
    {
        this.confirmNote = confirmNote;
    }

    public String getConfirmNote()
    {
        return confirmNote;
    }

    public void setConfirmBy(String confirmBy)
    {
        this.confirmBy = confirmBy;
    }

    public String getConfirmBy()
    {
        return confirmBy;
    }

    public void setConfirmTime(Date confirmTime)
    {
        this.confirmTime = confirmTime;
    }

    public Date getConfirmTime()
    {
        return confirmTime;
    }

    public void setPatientName(String patientName)
    {
        this.patientName = patientName;
    }

    public String getPatientName()
    {
        return patientName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("warningId", getWarningId())
            .append("userId", getUserId())
            .append("patientName", getPatientName())
            .append("warningType", getWarningType())
            .append("warningValue", getWarningValue())
            .append("warningLevel", getWarningLevel())
            .append("warningMsg", getWarningMsg())
            .append("thresholdRange", getThresholdRange())
            .append("isRead", getIsRead())
            .append("confirmStatus", getConfirmStatus())
            .append("confirmNote", getConfirmNote())
            .append("confirmBy", getConfirmBy())
            .append("confirmTime", getConfirmTime())
            .append("createTime", getCreateTime())
            .toString();
    }
}
