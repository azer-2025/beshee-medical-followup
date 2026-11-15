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
    @Excel(name = "患者ID")
    private Long patientId;

    /** 医生ID */
    @Excel(name = "医生ID")
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

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }
    public void setPlanId(Long planId) 
    {
        this.planId = planId;
    }

    public Long getPlanId() 
    {
        return planId;
    }
    public void setPatientId(Long patientId) 
    {
        this.patientId = patientId;
    }

    public Long getPatientId() 
    {
        return patientId;
    }
    public void setDoctorId(Long doctorId) 
    {
        this.doctorId = doctorId;
    }

    public Long getDoctorId() 
    {
        return doctorId;
    }
    public void setFeedback(String feedback) 
    {
        this.feedback = feedback;
    }

    public String getFeedback() 
    {
        return feedback;
    }
    public void setDoctorAdvice(String doctorAdvice) 
    {
        this.doctorAdvice = doctorAdvice;
    }

    public String getDoctorAdvice() 
    {
        return doctorAdvice;
    }
    public void setRecordDate(Date recordDate) 
    {
        this.recordDate = recordDate;
    }

    public Date getRecordDate() 
    {
        return recordDate;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDoctorName(String doctorName) 
    {
        this.doctorName = doctorName;
    }

    public String getDoctorName() 
    {
        return doctorName;
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
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("planId", getPlanId())
            .append("patientId", getPatientId())
            .append("patientName", getPatientName())
            .append("doctorId", getDoctorId())
            .append("doctorName", getDoctorName())
            .append("feedback", getFeedback())
            .append("doctorAdvice", getDoctorAdvice())
            .append("recordDate", getRecordDate())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
