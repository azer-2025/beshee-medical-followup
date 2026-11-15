package com.bishe.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bishe.common.core.domain.entity.SysUser;
import com.bishe.common.utils.DateUtils;
import com.bishe.common.utils.SecurityUtils;
import com.bishe.system.domain.HealthData;
import com.bishe.system.domain.HealthWarning;
import com.bishe.system.domain.vo.WarningThresholdSettingsVo;
import com.bishe.system.mapper.HealthDataMapper;
import com.bishe.system.service.IDoctorPatientService;
import com.bishe.system.service.IHealthDataService;
import com.bishe.system.service.IHealthWarningService;
import com.bishe.system.service.ISysUserService;
import com.bishe.system.service.IWarningThresholdService;

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

    @Autowired
    private IWarningThresholdService warningThresholdService;

    @Override
    public HealthData selectHealthDataById(Long dataId)
    {
        return healthDataMapper.selectHealthDataById(dataId);
    }

    @Override
    public List<HealthData> selectHealthDataList(HealthData healthData)
    {
        return healthDataMapper.selectHealthDataList(healthData);
    }

    @Override
    public int insertHealthData(HealthData healthData)
    {
        if (healthData.getUserId() == null)
        {
            healthData.setUserId(SecurityUtils.getUserId());
        }

        normalizeInput(healthData);
        if (healthData.getBloodSugar() != null && healthData.getBloodSugar() > 999.99)
        {
            healthData.setBloodSugar(999.99);
        }
        if (healthData.getHeight() != null && healthData.getHeight() > 999.99)
        {
            healthData.setHeight(999.99);
        }
        if (healthData.getWeight() != null && healthData.getWeight() > 999.99)
        {
            healthData.setWeight(999.99);
        }

        healthData.setCreateTime(DateUtils.getNowDate());
        int rows = healthDataMapper.insertHealthData(healthData);
        if (rows > 0)
        {
            try
            {
                checkAndSendWarning(healthData);
            }
            catch (Exception e)
            {
                System.err.println("Failed to generate smart warning: " + e.getMessage());
            }
        }
        return rows;
    }

    private void normalizeInput(HealthData healthData)
    {
        if (healthData.getTemperature() != null)
        {
            if (healthData.getTemperature() < 30)
            {
                healthData.setTemperature(30.0);
            }
            else if (healthData.getTemperature() > 45)
            {
                healthData.setTemperature(45.0);
            }
        }
    }

    private void checkAndSendWarning(HealthData data)
    {
        Long userId = data.getUserId();
        WarningThresholdSettingsVo thresholds = warningThresholdService.getEffectiveSettings();

        if (data.getBloodPressureSys() != null && data.getBloodPressureDia() != null)
        {
            int sys = data.getBloodPressureSys();
            int dia = data.getBloodPressureDia();
            if (sys > thresholds.getBpSysHigh() || sys < thresholds.getBpSysLow()
                || dia > thresholds.getBpDiaHigh() || dia < thresholds.getBpDiaLow())
            {
                String level = (sys > thresholds.getBpSysCritical() || dia > thresholds.getBpDiaCritical()) ? "3" : "2";
                String threshold = String.format("收缩压 %d-%d mmHg，舒张压 %d-%d mmHg",
                    thresholds.getBpSysLow(), thresholds.getBpSysHigh(), thresholds.getBpDiaLow(), thresholds.getBpDiaHigh());
                String msg = String.format("系统阈值判定：当前血压 %d/%d mmHg，参考范围 %s。建议医生复核后确认处理意见。",
                    sys, dia, threshold);
                createWarning(userId, "血压异常", sys + "/" + dia, msg, level, threshold);
            }
        }

        if (data.getBloodSugar() != null)
        {
            double sugar = data.getBloodSugar();
            if (sugar > thresholds.getBloodSugarHigh() || sugar < thresholds.getBloodSugarLow())
            {
                String level = (sugar > thresholds.getBloodSugarCriticalHigh() || sugar < thresholds.getBloodSugarCriticalLow()) ? "3" : "2";
                String threshold = String.format("%.1f-%.1f mmol/L", thresholds.getBloodSugarLow(), thresholds.getBloodSugarHigh());
                String msg = String.format("系统阈值判定：当前血糖 %.1f mmol/L，参考范围 %s。建议复测并由医生二次确认。",
                    sugar, threshold);
                createWarning(userId, "血糖异常", String.format("%.1f", sugar), msg, level, threshold);
            }
        }

        if (data.getHeartRate() != null)
        {
            int hr = data.getHeartRate();
            if (hr > thresholds.getHeartRateHigh() || hr < thresholds.getHeartRateLow())
            {
                String level = (hr > thresholds.getHeartRateCriticalHigh() || hr < thresholds.getHeartRateCriticalLow()) ? "3" : "2";
                String threshold = String.format("%d-%d bpm", thresholds.getHeartRateLow(), thresholds.getHeartRateHigh());
                String msg = String.format("系统阈值判定：当前心率 %d bpm，参考范围 %s。请结合症状由医生复核。", hr, threshold);
                createWarning(userId, "心率异常", String.valueOf(hr), msg, level, threshold);
            }
        }

        if (data.getHeight() != null && data.getWeight() != null && data.getHeight() > 0)
        {
            double heightM = data.getHeight() / 100.0;
            double bmi = data.getWeight() / (heightM * heightM);
            String threshold = String.format("BMI %.1f-%.1f，%.1f 及以上重点干预",
                thresholds.getBmiLow(), thresholds.getBmiHigh(), thresholds.getBmiObesity());
            if (bmi >= thresholds.getBmiObesity())
            {
                String msg = String.format("系统阈值判定：当前 BMI %.1f，明显高于参考范围 %s，建议医生确认后加强随访。", bmi, threshold);
                createWarning(userId, "体重超标", String.format("%.1f", bmi), msg, "2", threshold);
            }
            else if (bmi < thresholds.getBmiLow())
            {
                String msg = String.format("系统阈值判定：当前 BMI %.1f，低于参考范围 %s，建议医生确认营养与体重管理方案。", bmi, threshold);
                createWarning(userId, "体重过轻", String.format("%.1f", bmi), msg, "1", threshold);
            }
        }
    }

    private void createWarning(Long userId, String type, String value, String msg, String level, String threshold)
    {
        HealthWarning warning = new HealthWarning();
        warning.setUserId(userId);
        warning.setWarningType(type);
        warning.setWarningValue(value);
        warning.setWarningMsg(msg);
        warning.setWarningLevel(level);
        warning.setThresholdRange(threshold);
        warning.setIsRead("0");
        warning.setConfirmStatus("0");
        warning.setCreateTime(DateUtils.getNowDate());
        healthWarningService.insertHealthWarning(warning);

        try
        {
            SysUser patient = userService.selectUserById(userId);
            String patientName = patient != null ? patient.getNickName() : "未知患者";

            List<SysUser> doctors = doctorPatientService.selectDoctorByPatientId(userId);
            if (doctors != null && !doctors.isEmpty())
            {
                for (SysUser doctor : doctors)
                {
                    HealthWarning docWarning = new HealthWarning();
                    docWarning.setUserId(doctor.getUserId());
                    docWarning.setWarningType(type);
                    docWarning.setWarningValue(value);
                    docWarning.setWarningLevel(level);
                    docWarning.setWarningMsg("患者[" + patientName + "]触发预警: " + msg);
                    docWarning.setThresholdRange(threshold);
                    docWarning.setIsRead("0");
                    docWarning.setConfirmStatus("0");
                    docWarning.setCreateTime(DateUtils.getNowDate());
                    healthWarningService.insertHealthWarning(docWarning);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int updateHealthData(HealthData healthData)
    {
        normalizeInput(healthData);
        return healthDataMapper.updateHealthData(healthData);
    }

    @Override
    public int deleteHealthDataByIds(Long[] dataIds)
    {
        return healthDataMapper.deleteHealthDataByIds(dataIds);
    }

    @Override
    public int deleteHealthDataById(Long dataId)
    {
        return healthDataMapper.deleteHealthDataById(dataId);
    }
}
