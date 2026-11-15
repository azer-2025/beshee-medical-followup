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
    @Excel(name = "患者ID")
    private Long userId;

    /** 身高(cm) */
    @Excel(name = "身高(cm)")
    private Double height;

    /** 体重(kg) */
    @Excel(name = "体重(kg)")
    private Double weight;

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

    /** 测量时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "测量时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date measureTime;

    /** 患者姓名 */
    @Excel(name = "患者姓名")
    private String patientName;

    public void setDataId(Long dataId) 
    {
        this.dataId = dataId;
    }

    public Long getDataId() 
    {
        return dataId;
    }
    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }
    public void setHeight(Double height) 
    {
        this.height = height;
    }

    public Double getHeight() 
    {
        return height;
    }
    public void setWeight(Double weight) 
    {
        this.weight = weight;
    }

    public Double getWeight() 
    {
        return weight;
    }
    public void setBloodPressureSys(Integer bloodPressureSys) 
    {
        this.bloodPressureSys = bloodPressureSys;
    }

    public Integer getBloodPressureSys() 
    {
        return bloodPressureSys;
    }
    public void setBloodPressureDia(Integer bloodPressureDia) 
    {
        this.bloodPressureDia = bloodPressureDia;
    }

    public Integer getBloodPressureDia() 
    {
        return bloodPressureDia;
    }
    public void setHeartRate(Integer heartRate) 
    {
        this.heartRate = heartRate;
    }

    public Integer getHeartRate() 
    {
        return heartRate;
    }
    public void setBloodSugar(Double bloodSugar) 
    {
        this.bloodSugar = bloodSugar;
    }

    public Double getBloodSugar() 
    {
        return bloodSugar;
    }
    public void setReportImage(String reportImage) 
    {
        this.reportImage = reportImage;
    }

    public String getReportImage() 
    {
        return reportImage;
    }
    public void setMeasureTime(Date measureTime) 
    {
        this.measureTime = measureTime;
    }

    public Date getMeasureTime() 
    {
        return measureTime;
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
            .append("dataId", getDataId())
            .append("userId", getUserId())
            .append("patientName", getPatientName())
            .append("height", getHeight())
            .append("weight", getWeight())
            .append("bloodPressureSys", getBloodPressureSys())
            .append("bloodPressureDia", getBloodPressureDia())
            .append("heartRate", getHeartRate())
            .append("bloodSugar", getBloodSugar())
            .append("reportImage", getReportImage())
            .append("measureTime", getMeasureTime())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
