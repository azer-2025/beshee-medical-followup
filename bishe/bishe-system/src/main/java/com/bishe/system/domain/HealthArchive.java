package com.bishe.system.domain;

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
    @Excel(name = "患者ID", isExport = false)
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

    /** 病种 */
    @Excel(name = "病种")
    private String diseaseType;

    /** 年龄阶段 */
    @Excel(name = "年龄阶段")
    private String ageStage;

    /** 风险等级 */
    @Excel(name = "风险等级")
    private String riskLevel;

    /** 档案标签 */
    @Excel(name = "档案标签")
    private String archiveTags;

    /** 诊断记录 */
    @Excel(name = "诊断记录")
    private String diagnosisRecord;

    /** 检查报告 */
    @Excel(name = "检查报告")
    private String examReport;

    /** 训练计划 */
    @Excel(name = "训练计划")
    private String trainingPlan;

    /** 用药建议 */
    @Excel(name = "用药建议")
    private String medicationAdvice;

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

    public String getDiseaseType()
    {
        return diseaseType;
    }

    public void setDiseaseType(String diseaseType)
    {
        this.diseaseType = diseaseType;
    }

    public String getAgeStage()
    {
        return ageStage;
    }

    public void setAgeStage(String ageStage)
    {
        this.ageStage = ageStage;
    }

    public String getRiskLevel()
    {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel)
    {
        this.riskLevel = riskLevel;
    }

    public String getArchiveTags()
    {
        return archiveTags;
    }

    public void setArchiveTags(String archiveTags)
    {
        this.archiveTags = archiveTags;
    }

    public String getDiagnosisRecord()
    {
        return diagnosisRecord;
    }

    public void setDiagnosisRecord(String diagnosisRecord)
    {
        this.diagnosisRecord = diagnosisRecord;
    }

    public String getExamReport()
    {
        return examReport;
    }

    public void setExamReport(String examReport)
    {
        this.examReport = examReport;
    }

    public String getTrainingPlan()
    {
        return trainingPlan;
    }

    public void setTrainingPlan(String trainingPlan)
    {
        this.trainingPlan = trainingPlan;
    }

    public String getMedicationAdvice()
    {
        return medicationAdvice;
    }

    public void setMedicationAdvice(String medicationAdvice)
    {
        this.medicationAdvice = medicationAdvice;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("archiveId", getArchiveId())
            .append("userId", getUserId())
            .append("patientName", getPatientName())
            .append("diseaseType", getDiseaseType())
            .append("ageStage", getAgeStage())
            .append("riskLevel", getRiskLevel())
            .append("archiveTags", getArchiveTags())
            .append("basicInfo", getBasicInfo())
            .append("medicalHistory", getMedicalHistory())
            .append("allergyHistory", getAllergyHistory())
            .append("diagnosisRecord", getDiagnosisRecord())
            .append("examReport", getExamReport())
            .append("trainingPlan", getTrainingPlan())
            .append("medicationAdvice", getMedicationAdvice())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
