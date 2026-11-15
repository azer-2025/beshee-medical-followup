package com.bishe.system.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.bishe.common.annotation.Excel;
import com.bishe.common.core.domain.BaseEntity;

/**
 * 电子健康档案对象 health_archive
 * 
 * @author bishe
 * @date 2025-05-22
 */
public class HealthArchive extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 档案ID */
    private Long archiveId;

    /** 患者ID */
    @Excel(name = "患者ID")
    private Long userId;

    /** 基本信息 */
    @Excel(name = "基本信息")
    private String basicInfo;

    /** 既往病史 */
    @Excel(name = "既往病史")
    private String medicalHistory;

    /** 过敏史 */
    @Excel(name = "过敏史")
    private String allergyHistory;

    /** 患者姓名 */
    @Excel(name = "患者姓名")
    private String patientName;

    public void setArchiveId(Long archiveId) 
    {
        this.archiveId = archiveId;
    }

    public Long getArchiveId() 
    {
        return archiveId;
    }
    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }
    public void setBasicInfo(String basicInfo) 
    {
        this.basicInfo = basicInfo;
    }

    public String getBasicInfo() 
    {
        return basicInfo;
    }
    public void setMedicalHistory(String medicalHistory) 
    {
        this.medicalHistory = medicalHistory;
    }

    public String getMedicalHistory() 
    {
        return medicalHistory;
    }
    public void setAllergyHistory(String allergyHistory) 
    {
        this.allergyHistory = allergyHistory;
    }

    public String getAllergyHistory() 
    {
        return allergyHistory;
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
            .append("archiveId", getArchiveId())
            .append("userId", getUserId())
            .append("patientName", getPatientName())
            .append("basicInfo", getBasicInfo())
            .append("medicalHistory", getMedicalHistory())
            .append("allergyHistory", getAllergyHistory())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
