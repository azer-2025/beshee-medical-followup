package com.bishe.system.service.impl;

import java.util.List;
import com.bishe.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bishe.system.mapper.HealthDataMapper;
import com.bishe.system.domain.HealthData;
import com.bishe.system.domain.HealthWarning;
import com.bishe.system.service.IHealthDataService;
import com.bishe.system.service.IHealthWarningService;

import com.bishe.system.service.IDoctorPatientService;
import com.bishe.common.core.domain.entity.SysUser;
import com.bishe.system.service.ISysUserService;

/**
 * 健康数据采集Service业务层处理
 * 
 * @author bishe
 * @date 2025-05-22
 */
@Service
public class HealthDataServiceImpl implements IHealthDataService 
{
    @Autowired
    private HealthDataMapper healthDataMapper;

    @Autowired
    private IHealthWarningService healthWarningService;

    @Autowired
    private IDoctorPatientService doctorPatientService;

    @Autowired
    private ISysUserService userService;

    /**
     * 查询健康数据采集
     * 
     * @param dataId 健康数据采集主键
     * @return 健康数据采集
     */
    @Override
    public HealthData selectHealthDataById(Long dataId)
    {
        return healthDataMapper.selectHealthDataById(dataId);
    }

    /**
     * 查询健康数据采集列表
     * 
     * @param healthData 健康数据采集
     * @return 健康数据采集
     */
    @Override
    public List<HealthData> selectHealthDataList(HealthData healthData)
    {
        return healthDataMapper.selectHealthDataList(healthData);
    }

    @Override
    public int insertHealthData(HealthData healthData)
    {
        // 1. Ensure patient ID is present (fallback to current user if missing)
        if (healthData.getUserId() == null) {
            healthData.setUserId(com.bishe.common.utils.SecurityUtils.getUserId());
        }

        // 2. Data range protection
        if (healthData.getBloodSugar() != null && healthData.getBloodSugar() > 999.99) healthData.setBloodSugar(999.99);
        if (healthData.getHeight() != null && healthData.getHeight() > 999.99) healthData.setHeight(999.99);
        if (healthData.getWeight() != null && healthData.getWeight() > 999.99) healthData.setWeight(999.99);
        
        healthData.setCreateTime(DateUtils.getNowDate());
        int rows = healthDataMapper.insertHealthData(healthData);
        
        // 3. Trigger smart warning if insert was successful
        if (rows > 0) {
            try {
                checkAndSendWarning(healthData);
            } catch (Exception e) {
                // Log error but don't crash the main process
                System.err.println("Failed to generate smart warning: " + e.getMessage());
            }
        }
        return rows;
    }

    /**
     * 检查数据并发送预警 (智能预警算法)
     */
    private void checkAndSendWarning(HealthData data) {
        Long userId = data.getUserId();
        
        // 1. 血压检查 (收缩压 90-140, 舒张压 60-90)
        if (data.getBloodPressureSys() != null && data.getBloodPressureDia() != null) {
            int sys = data.getBloodPressureSys();
            int dia = data.getBloodPressureDia();
            if (sys > 140 || sys < 90 || dia > 90 || dia < 60) {
                String level = (sys > 160 || dia > 100) ? "3" : "2";
                createWarning(userId, "血压异常", sys + "/" + dia, 
                    String.format("智能识别到血压异常: %d/%d mmHg。请注意休息，如持续不适请联系医生。", sys, dia), level);
            }
        }
        
        // 2. 血糖检查 (空腹 3.9-7.0)
        if (data.getBloodSugar() != null) {
            double sugar = data.getBloodSugar();
            if (sugar > 7.0 || sugar < 3.9) {
                String level = (sugar > 11.1 || sugar < 2.8) ? "3" : "2";
                createWarning(userId, "血糖异常", String.valueOf(sugar), 
                    String.format("智能检测到血糖值 %.1f mmol/L 偏离正常范围。建议复测并调整饮食。", sugar), level);
            }
        }
        
        // 3. 心率检查 (60-100)
        if (data.getHeartRate() != null) {
            int hr = data.getHeartRate();
            if (hr > 100 || hr < 60) {
                createWarning(userId, "心率异常", String.valueOf(hr), 
                    String.format("智能检测到心率波动: %d bpm。请保持平静并观察。", hr), "2");
            }
        }

        // 4. BMI 智能计算 (身高/体重)
        if (data.getHeight() != null && data.getWeight() != null && data.getHeight() > 0) {
            double heightM = data.getHeight() / 100.0;
            double bmi = data.getWeight() / (heightM * heightM);
            if (bmi > 28.0) {
                createWarning(userId, "体重超标", String.format("%.1f", bmi), 
                    String.format("智能预警：您的BMI指数为 %.1f，属于肥胖范畴，建议加强随访管理。", bmi), "2");
            } else if (bmi < 18.5) {
                createWarning(userId, "体重过轻", String.format("%.1f", bmi), 
                    String.format("智能预警：您的BMI指数为 %.1f，体重过轻，请注意营养摄入。", bmi), "1");
            }
        }
    }

    private void createWarning(Long userId, String type, String value, String msg, String level) {
        // 1. Send to Patient
        HealthWarning warning = new HealthWarning();
        warning.setUserId(userId);
        warning.setWarningType(type);
        warning.setWarningValue(value);
        warning.setWarningMsg(msg);
        warning.setWarningLevel(level);
        warning.setIsRead("0"); // 默认未读
        warning.setCreateTime(DateUtils.getNowDate());
        healthWarningService.insertHealthWarning(warning);

        // 2. Send to Associated Doctors (Double Send)
        try {
            SysUser patient = userService.selectUserById(userId);
            String patientName = (patient != null) ? patient.getNickName() : "未知患者";

            List<SysUser> doctors = doctorPatientService.selectDoctorByPatientId(userId);
            if (doctors != null && !doctors.isEmpty()) {
                for (SysUser doctor : doctors) {
                    HealthWarning docWarning = new HealthWarning();
                    docWarning.setUserId(doctor.getUserId()); // Send to Doctor
                    docWarning.setWarningType(type);
                    docWarning.setWarningValue(value);
                    docWarning.setWarningLevel(level);
                    // Add context to message
                    docWarning.setWarningMsg("患者[" + patientName + "]触发预警: " + msg); 
                    docWarning.setIsRead("0");
                    docWarning.setCreateTime(DateUtils.getNowDate());
                    healthWarningService.insertHealthWarning(docWarning);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改健康数据采集
     * 
     * @param healthData 健康数据采集
     * @return 结果
     */
    @Override
    public int updateHealthData(HealthData healthData)
    {
        return healthDataMapper.updateHealthData(healthData);
    }

    /**
     * 批量删除健康数据采集
     * 
     * @param dataIds 需要删除的健康数据采集主键
     * @return 结果
     */
    @Override
    public int deleteHealthDataByIds(Long[] dataIds)
    {
        return healthDataMapper.deleteHealthDataByIds(dataIds);
    }

    /**
     * 删除健康数据采集信息
     * 
     * @param dataId 健康数据采集主键
     * @return 结果
     */
    @Override
    public int deleteHealthDataById(Long dataId)
    {
        return healthDataMapper.deleteHealthDataById(dataId);
    }
}
