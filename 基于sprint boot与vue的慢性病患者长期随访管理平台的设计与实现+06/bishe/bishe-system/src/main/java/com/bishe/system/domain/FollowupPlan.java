package com.bishe.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.bishe.common.annotation.Excel;
import com.bishe.common.core.domain.BaseEntity;

/**
 * 随访计划对象 followup_plan
 * 
 * @author bishe
 * @date 2025-05-22
 */
public class FollowupPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 计划ID */
    private Long planId;

    /** 医生ID */
    @Excel(name = "医生ID")
    private Long doctorId;

    /** 患者ID */
    @Excel(name = "患者ID")
    private Long patientId;

    /** 计划标题 */
    @Excel(name = "计划标题")
    private String planTitle;

    /** 随访内容/指导 */
    @Excel(name = "随访内容/指导")
    private String planContent;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 频率 */
    @Excel(name = "频率")
    private String frequency;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=进行中,1=已完成,2=已取消")
    private String status;

    /** 医生姓名 */
    @Excel(name = "医生姓名")
    private String doctorName;

    /** 患者姓名 */
    @Excel(name = "患者姓名")
    private String patientName;

    public void setPlanId(Long planId) 
    {
        this.planId = planId;
    }

    public Long getPlanId() 
    {
        return planId;
    }
    public void setDoctorId(Long doctorId) 
    {
        this.doctorId = doctorId;
    }

    public Long getDoctorId() 
    {
        return doctorId;
    }
    public void setPatientId(Long patientId) 
    {
        this.patientId = patientId;
    }

    public Long getPatientId() 
    {
        return patientId;
    }
    public void setPlanTitle(String planTitle) 
    {
        this.planTitle = planTitle;
    }

    public String getPlanTitle() 
    {
        return planTitle;
    }
    public void setPlanContent(String planContent) 
    {
        this.planContent = planContent;
    }

    public String getPlanContent() 
    {
        return planContent;
    }
    public void setStartDate(Date startDate) 
    {
        this.startDate = startDate;
    }

    public Date getStartDate() 
    {
        return startDate;
    }
    public void setEndDate(Date endDate) 
    {
        this.endDate = endDate;
    }

    public Date getEndDate() 
    {
        return endDate;
    }
    public void setFrequency(String frequency) 
    {
        this.frequency = frequency;
    }

    public String getFrequency() 
    {
        return frequency;
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
            .append("planId", getPlanId())
            .append("doctorId", getDoctorId())
            .append("doctorName", getDoctorName())
            .append("patientId", getPatientId())
            .append("patientName", getPatientName())
            .append("planTitle", getPlanTitle())
            .append("planContent", getPlanContent())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("frequency", getFrequency())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
