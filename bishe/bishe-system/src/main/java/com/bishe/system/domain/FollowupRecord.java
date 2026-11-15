package com.bishe.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.bishe.common.annotation.Excel;
import com.bishe.common.core.domain.BaseEntity;

/**
 * 随访记录对象 followup_record
 *
 * @author bishe
 * @date 2025-05-22
 */
public class FollowupRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 关联计划ID */
    @Excel(name = "关联计划ID")
    private Long planId;

    /** 患者ID */
    @Excel(name = "患者ID", isExport = false)
    private Long patientId;

    /** 医生ID */
    @Excel(name = "医生ID", isExport = false)
    private Long doctorId;

    /** 患者反馈 */
    @Excel(name = "患者反馈")
    private String feedback;

    /** 医生建议/小结 */
    @Excel(name = "医生建议/小结")
    private String doctorAdvice;

    /** 随访日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "随访日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date recordDate;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=待随访,1=已反馈,2=已完成")
    private String status;

    /** 医生姓名 */
    @Excel(name = "医生姓名")
    private String doctorName;

    /** 患者姓名 */
    @Excel(name = "患者姓名")
    private String patientName;

    /** 计划标题 */
    @Excel(name = "计划标题")
    private String planTitle;

    /** 模块类型 */
    @Excel(name = "提醒模块")
    private String moduleType;

    /** 需要反馈 */
    private String requireFeedback;

    /** 需要拍照 */
    private String requirePhoto;

    /** 服药结果 */
    @Excel(name = "服药结果")
    private String medicationResult;

    /** 康复训练图片 */
    @Excel(name = "康复训练图片")
    private String rehabPhoto;

    /** 提醒次数 */
    @Excel(name = "提醒次数")
    private Integer reminderCount;

    /** 最近一次提醒时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近提醒时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastReminderTime;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public Long getPlanId()
    {
        return planId;
    }

    public void setPlanId(Long planId)
    {
        this.planId = planId;
    }

    public Long getPatientId()
    {
        return patientId;
    }

    public void setPatientId(Long patientId)
    {
        this.patientId = patientId;
    }

    public Long getDoctorId()
    {
        return doctorId;
    }

    public void setDoctorId(Long doctorId)
    {
        this.doctorId = doctorId;
    }

    public String getFeedback()
    {
        return feedback;
    }

    public void setFeedback(String feedback)
    {
        this.feedback = feedback;
    }

    public String getDoctorAdvice()
    {
        return doctorAdvice;
    }

    public void setDoctorAdvice(String doctorAdvice)
    {
        this.doctorAdvice = doctorAdvice;
    }

    public Date getRecordDate()
    {
        return recordDate;
    }

    public void setRecordDate(Date recordDate)
    {
        this.recordDate = recordDate;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDoctorName()
    {
        return doctorName;
    }

    public void setDoctorName(String doctorName)
    {
        this.doctorName = doctorName;
    }

    public String getPatientName()
    {
        return patientName;
    }

    public void setPatientName(String patientName)
    {
        this.patientName = patientName;
    }

    public String getPlanTitle()
    {
        return planTitle;
    }

    public void setPlanTitle(String planTitle)
    {
        this.planTitle = planTitle;
    }

    public String getModuleType()
    {
        return moduleType;
    }

    public void setModuleType(String moduleType)
    {
        this.moduleType = moduleType;
    }

    public String getRequireFeedback()
    {
        return requireFeedback;
    }

    public void setRequireFeedback(String requireFeedback)
    {
        this.requireFeedback = requireFeedback;
    }

    public String getRequirePhoto()
    {
        return requirePhoto;
    }

    public void setRequirePhoto(String requirePhoto)
    {
        this.requirePhoto = requirePhoto;
    }

    public String getMedicationResult()
    {
        return medicationResult;
    }

    public void setMedicationResult(String medicationResult)
    {
        this.medicationResult = medicationResult;
    }

    public String getRehabPhoto()
    {
        return rehabPhoto;
    }

    public void setRehabPhoto(String rehabPhoto)
    {
        this.rehabPhoto = rehabPhoto;
    }

    public Integer getReminderCount()
    {
        return reminderCount;
    }

    public void setReminderCount(Integer reminderCount)
    {
        this.reminderCount = reminderCount;
    }

    public Date getLastReminderTime()
    {
        return lastReminderTime;
    }

    public void setLastReminderTime(Date lastReminderTime)
    {
        this.lastReminderTime = lastReminderTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("planId", getPlanId())
            .append("planTitle", getPlanTitle())
            .append("patientId", getPatientId())
            .append("patientName", getPatientName())
            .append("moduleType", getModuleType())
            .append("doctorId", getDoctorId())
            .append("doctorName", getDoctorName())
            .append("feedback", getFeedback())
            .append("doctorAdvice", getDoctorAdvice())
            .append("medicationResult", getMedicationResult())
            .append("rehabPhoto", getRehabPhoto())
            .append("reminderCount", getReminderCount())
            .append("lastReminderTime", getLastReminderTime())
            .append("recordDate", getRecordDate())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
