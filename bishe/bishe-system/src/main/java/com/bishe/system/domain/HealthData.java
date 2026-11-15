package com.bishe.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.bishe.common.annotation.Excel;
import com.bishe.common.core.domain.BaseEntity;

/**
 * 健康数据采集对象 health_data
 *
 * @author bishe
 * @date 2025-05-22
 */
public class HealthData extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 数据ID */
    private Long dataId;

    /** 患者ID */
    @Excel(name = "患者ID", isExport = false)
    private Long userId;

    /** 身高(cm) */
    @Excel(name = "身高(cm)")
    private Double height;

    /** 体重(kg) */
    @Excel(name = "体重(kg)")
    private Double weight;

    /** 体温(℃) */
    @Excel(name = "体温(℃)")
    private Double temperature;

    /** 收缩压(mmHg) */
    @Excel(name = "收缩压(mmHg)")
    private Integer bloodPressureSys;

    /** 舒张压(mmHg) */
    @Excel(name = "舒张压(mmHg)")
    private Integer bloodPressureDia;

    /** 心率(bpm) */
    @Excel(name = "心率(bpm)")
    private Integer heartRate;

    /** 血糖(mmol/L) */
    @Excel(name = "血糖(mmol/L)")
    private Double bloodSugar;

    /** 体检报告图片URL */
    @Excel(name = "体检报告图片URL")
    private String reportImage;

    /** 康复训练图片 */
    @Excel(name = "康复训练图片")
    private String rehabPhoto;

    /** 测量时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "测量时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date measureTime;

    /** 患者姓名 */
    @Excel(name = "患者姓名")
    private String patientName;

    /** BMI */
    @Excel(name = "BMI")
    private String bmiValue;

    /** 体重参考范围 */
    @Excel(name = "体重参考范围")
    private String weightReference;

    /** 血压参考范围 */
    @Excel(name = "血压参考范围")
    private String bloodPressureReference;

    /** 心率参考范围 */
    @Excel(name = "心率参考范围")
    private String heartRateReference;

    /** 血糖参考范围 */
    @Excel(name = "血糖参考范围")
    private String bloodSugarReference;

    /** BMI参考范围 */
    @Excel(name = "BMI参考范围")
    private String bmiReference;

    /** 体检结论 */
    @Excel(name = "体检结论")
    private String healthAssessment;

    /** 异常提示 */
    @Excel(name = "异常提示")
    private String abnormalItems;

    public Long getDataId()
    {
        return dataId;
    }

    public void setDataId(Long dataId)
    {
        this.dataId = dataId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Double getHeight()
    {
        return height;
    }

    public void setHeight(Double height)
    {
        this.height = height;
    }

    public Double getWeight()
    {
        return weight;
    }

    public void setWeight(Double weight)
    {
        this.weight = weight;
    }

    public Double getTemperature()
    {
        return temperature;
    }

    public void setTemperature(Double temperature)
    {
        this.temperature = temperature;
    }

    public Integer getBloodPressureSys()
    {
        return bloodPressureSys;
    }

    public void setBloodPressureSys(Integer bloodPressureSys)
    {
        this.bloodPressureSys = bloodPressureSys;
    }

    public Integer getBloodPressureDia()
    {
        return bloodPressureDia;
    }

    public void setBloodPressureDia(Integer bloodPressureDia)
    {
        this.bloodPressureDia = bloodPressureDia;
    }

    public Integer getHeartRate()
    {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate)
    {
        this.heartRate = heartRate;
    }

    public Double getBloodSugar()
    {
        return bloodSugar;
    }

    public void setBloodSugar(Double bloodSugar)
    {
        this.bloodSugar = bloodSugar;
    }

    public String getReportImage()
    {
        return reportImage;
    }

    public void setReportImage(String reportImage)
    {
        this.reportImage = reportImage;
    }

    public String getRehabPhoto()
    {
        return rehabPhoto;
    }

    public void setRehabPhoto(String rehabPhoto)
    {
        this.rehabPhoto = rehabPhoto;
    }

    public Date getMeasureTime()
    {
        return measureTime;
    }

    public void setMeasureTime(Date measureTime)
    {
        this.measureTime = measureTime;
    }

    public String getPatientName()
    {
        return patientName;
    }

    public void setPatientName(String patientName)
    {
        this.patientName = patientName;
    }

    public String getBmiValue()
    {
        return bmiValue;
    }

    public void setBmiValue(String bmiValue)
    {
        this.bmiValue = bmiValue;
    }

    public String getWeightReference()
    {
        return weightReference;
    }

    public void setWeightReference(String weightReference)
    {
        this.weightReference = weightReference;
    }

    public String getBloodPressureReference()
    {
        return bloodPressureReference;
    }

    public void setBloodPressureReference(String bloodPressureReference)
    {
        this.bloodPressureReference = bloodPressureReference;
    }

    public String getHeartRateReference()
    {
        return heartRateReference;
    }

    public void setHeartRateReference(String heartRateReference)
    {
        this.heartRateReference = heartRateReference;
    }

    public String getBloodSugarReference()
    {
        return bloodSugarReference;
    }

    public void setBloodSugarReference(String bloodSugarReference)
    {
        this.bloodSugarReference = bloodSugarReference;
    }

    public String getBmiReference()
    {
        return bmiReference;
    }

    public void setBmiReference(String bmiReference)
    {
        this.bmiReference = bmiReference;
    }

    public String getHealthAssessment()
    {
        return healthAssessment;
    }

    public void setHealthAssessment(String healthAssessment)
    {
        this.healthAssessment = healthAssessment;
    }

    public String getAbnormalItems()
    {
        return abnormalItems;
    }

    public void setAbnormalItems(String abnormalItems)
    {
        this.abnormalItems = abnormalItems;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("dataId", getDataId())
            .append("userId", getUserId())
            .append("patientName", getPatientName())
            .append("height", getHeight())
            .append("weight", getWeight())
            .append("temperature", getTemperature())
            .append("bloodPressureSys", getBloodPressureSys())
            .append("bloodPressureDia", getBloodPressureDia())
            .append("heartRate", getHeartRate())
            .append("bloodSugar", getBloodSugar())
            .append("reportImage", getReportImage())
            .append("rehabPhoto", getRehabPhoto())
            .append("measureTime", getMeasureTime())
            .append("bmiValue", getBmiValue())
            .append("weightReference", getWeightReference())
            .append("bloodPressureReference", getBloodPressureReference())
            .append("heartRateReference", getHeartRateReference())
            .append("bloodSugarReference", getBloodSugarReference())
            .append("bmiReference", getBmiReference())
            .append("healthAssessment", getHealthAssessment())
            .append("abnormalItems", getAbnormalItems())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
