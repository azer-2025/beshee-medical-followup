package com.bishe.system.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bishe.common.exception.ServiceException;
import com.bishe.system.domain.SysConfig;
import com.bishe.system.domain.vo.WarningThresholdSettingsVo;
import com.bishe.system.mapper.SysConfigMapper;
import com.bishe.system.service.ISysConfigService;
import com.bishe.system.service.IWarningThresholdService;

@Service
public class WarningThresholdServiceImpl implements IWarningThresholdService
{
    private static final String KEY_BP_SYS_LOW = "health.warning.threshold.bp.sys.low";
    private static final String KEY_BP_SYS_HIGH = "health.warning.threshold.bp.sys.high";
    private static final String KEY_BP_SYS_CRITICAL = "health.warning.threshold.bp.sys.critical";
    private static final String KEY_BP_DIA_LOW = "health.warning.threshold.bp.dia.low";
    private static final String KEY_BP_DIA_HIGH = "health.warning.threshold.bp.dia.high";
    private static final String KEY_BP_DIA_CRITICAL = "health.warning.threshold.bp.dia.critical";
    private static final String KEY_SUGAR_LOW = "health.warning.threshold.sugar.low";
    private static final String KEY_SUGAR_HIGH = "health.warning.threshold.sugar.high";
    private static final String KEY_SUGAR_CRITICAL_LOW = "health.warning.threshold.sugar.critical.low";
    private static final String KEY_SUGAR_CRITICAL_HIGH = "health.warning.threshold.sugar.critical.high";
    private static final String KEY_HEART_RATE_LOW = "health.warning.threshold.heart.low";
    private static final String KEY_HEART_RATE_HIGH = "health.warning.threshold.heart.high";
    private static final String KEY_HEART_RATE_CRITICAL_LOW = "health.warning.threshold.heart.critical.low";
    private static final String KEY_HEART_RATE_CRITICAL_HIGH = "health.warning.threshold.heart.critical.high";
    private static final String KEY_BMI_LOW = "health.warning.threshold.bmi.low";
    private static final String KEY_BMI_HIGH = "health.warning.threshold.bmi.high";
    private static final String KEY_BMI_OBESITY = "health.warning.threshold.bmi.obesity";

    private static final int DEFAULT_BP_SYS_LOW = 90;
    private static final int DEFAULT_BP_SYS_HIGH = 140;
    private static final int DEFAULT_BP_SYS_CRITICAL = 160;
    private static final int DEFAULT_BP_DIA_LOW = 60;
    private static final int DEFAULT_BP_DIA_HIGH = 90;
    private static final int DEFAULT_BP_DIA_CRITICAL = 100;
    private static final double DEFAULT_SUGAR_LOW = 3.9D;
    private static final double DEFAULT_SUGAR_HIGH = 7.0D;
    private static final double DEFAULT_SUGAR_CRITICAL_LOW = 2.8D;
    private static final double DEFAULT_SUGAR_CRITICAL_HIGH = 11.1D;
    private static final int DEFAULT_HEART_RATE_LOW = 60;
    private static final int DEFAULT_HEART_RATE_HIGH = 100;
    private static final int DEFAULT_HEART_RATE_CRITICAL_LOW = 50;
    private static final int DEFAULT_HEART_RATE_CRITICAL_HIGH = 120;
    private static final double DEFAULT_BMI_LOW = 18.5D;
    private static final double DEFAULT_BMI_HIGH = 23.9D;
    private static final double DEFAULT_BMI_OBESITY = 28.0D;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private SysConfigMapper configMapper;

    @Override
    public WarningThresholdSettingsVo getStoredSettings()
    {
        WarningThresholdSettingsVo settings = new WarningThresholdSettingsVo();
        settings.setBpSysLow(parseInteger(configService.selectConfigByKey(KEY_BP_SYS_LOW)));
        settings.setBpSysHigh(parseInteger(configService.selectConfigByKey(KEY_BP_SYS_HIGH)));
        settings.setBpSysCritical(parseInteger(configService.selectConfigByKey(KEY_BP_SYS_CRITICAL)));
        settings.setBpDiaLow(parseInteger(configService.selectConfigByKey(KEY_BP_DIA_LOW)));
        settings.setBpDiaHigh(parseInteger(configService.selectConfigByKey(KEY_BP_DIA_HIGH)));
        settings.setBpDiaCritical(parseInteger(configService.selectConfigByKey(KEY_BP_DIA_CRITICAL)));
        settings.setBloodSugarLow(parseDouble(configService.selectConfigByKey(KEY_SUGAR_LOW)));
        settings.setBloodSugarHigh(parseDouble(configService.selectConfigByKey(KEY_SUGAR_HIGH)));
        settings.setBloodSugarCriticalLow(parseDouble(configService.selectConfigByKey(KEY_SUGAR_CRITICAL_LOW)));
        settings.setBloodSugarCriticalHigh(parseDouble(configService.selectConfigByKey(KEY_SUGAR_CRITICAL_HIGH)));
        settings.setHeartRateLow(parseInteger(configService.selectConfigByKey(KEY_HEART_RATE_LOW)));
        settings.setHeartRateHigh(parseInteger(configService.selectConfigByKey(KEY_HEART_RATE_HIGH)));
        settings.setHeartRateCriticalLow(parseInteger(configService.selectConfigByKey(KEY_HEART_RATE_CRITICAL_LOW)));
        settings.setHeartRateCriticalHigh(parseInteger(configService.selectConfigByKey(KEY_HEART_RATE_CRITICAL_HIGH)));
        settings.setBmiLow(parseDouble(configService.selectConfigByKey(KEY_BMI_LOW)));
        settings.setBmiHigh(parseDouble(configService.selectConfigByKey(KEY_BMI_HIGH)));
        settings.setBmiObesity(parseDouble(configService.selectConfigByKey(KEY_BMI_OBESITY)));
        return settings;
    }

    @Override
    public WarningThresholdSettingsVo getEffectiveSettings()
    {
        return mergeWithDefaults(getStoredSettings());
    }

    @Override
    public Map<String, String> getDefaultThresholdTexts()
    {
        return buildThresholdTexts(createDefaultSettings());
    }

    @Override
    public Map<String, String> getEffectiveThresholdTexts()
    {
        return buildThresholdTexts(getEffectiveSettings());
    }

    @Override
    public void saveSettings(WarningThresholdSettingsVo settings, String operator)
    {
        WarningThresholdSettingsVo target = settings == null ? new WarningThresholdSettingsVo() : settings;
        validateSettings(mergeWithDefaults(target));

        saveConfig(KEY_BP_SYS_LOW, "智能预警-收缩压下限", toValue(target.getBpSysLow()), operator);
        saveConfig(KEY_BP_SYS_HIGH, "智能预警-收缩压上限", toValue(target.getBpSysHigh()), operator);
        saveConfig(KEY_BP_SYS_CRITICAL, "智能预警-收缩压高危阈值", toValue(target.getBpSysCritical()), operator);
        saveConfig(KEY_BP_DIA_LOW, "智能预警-舒张压下限", toValue(target.getBpDiaLow()), operator);
        saveConfig(KEY_BP_DIA_HIGH, "智能预警-舒张压上限", toValue(target.getBpDiaHigh()), operator);
        saveConfig(KEY_BP_DIA_CRITICAL, "智能预警-舒张压高危阈值", toValue(target.getBpDiaCritical()), operator);
        saveConfig(KEY_SUGAR_LOW, "智能预警-血糖下限", toValue(target.getBloodSugarLow()), operator);
        saveConfig(KEY_SUGAR_HIGH, "智能预警-血糖上限", toValue(target.getBloodSugarHigh()), operator);
        saveConfig(KEY_SUGAR_CRITICAL_LOW, "智能预警-血糖低危高危阈值", toValue(target.getBloodSugarCriticalLow()), operator);
        saveConfig(KEY_SUGAR_CRITICAL_HIGH, "智能预警-血糖高危阈值", toValue(target.getBloodSugarCriticalHigh()), operator);
        saveConfig(KEY_HEART_RATE_LOW, "智能预警-心率下限", toValue(target.getHeartRateLow()), operator);
        saveConfig(KEY_HEART_RATE_HIGH, "智能预警-心率上限", toValue(target.getHeartRateHigh()), operator);
        saveConfig(KEY_HEART_RATE_CRITICAL_LOW, "智能预警-心率低危高危阈值", toValue(target.getHeartRateCriticalLow()), operator);
        saveConfig(KEY_HEART_RATE_CRITICAL_HIGH, "智能预警-心率高危阈值", toValue(target.getHeartRateCriticalHigh()), operator);
        saveConfig(KEY_BMI_LOW, "智能预警-BMI下限", toValue(target.getBmiLow()), operator);
        saveConfig(KEY_BMI_HIGH, "智能预警-BMI上限", toValue(target.getBmiHigh()), operator);
        saveConfig(KEY_BMI_OBESITY, "智能预警-BMI肥胖阈值", toValue(target.getBmiObesity()), operator);
        configService.resetConfigCache();
    }

    private WarningThresholdSettingsVo createDefaultSettings()
    {
        WarningThresholdSettingsVo settings = new WarningThresholdSettingsVo();
        settings.setBpSysLow(DEFAULT_BP_SYS_LOW);
        settings.setBpSysHigh(DEFAULT_BP_SYS_HIGH);
        settings.setBpSysCritical(DEFAULT_BP_SYS_CRITICAL);
        settings.setBpDiaLow(DEFAULT_BP_DIA_LOW);
        settings.setBpDiaHigh(DEFAULT_BP_DIA_HIGH);
        settings.setBpDiaCritical(DEFAULT_BP_DIA_CRITICAL);
        settings.setBloodSugarLow(DEFAULT_SUGAR_LOW);
        settings.setBloodSugarHigh(DEFAULT_SUGAR_HIGH);
        settings.setBloodSugarCriticalLow(DEFAULT_SUGAR_CRITICAL_LOW);
        settings.setBloodSugarCriticalHigh(DEFAULT_SUGAR_CRITICAL_HIGH);
        settings.setHeartRateLow(DEFAULT_HEART_RATE_LOW);
        settings.setHeartRateHigh(DEFAULT_HEART_RATE_HIGH);
        settings.setHeartRateCriticalLow(DEFAULT_HEART_RATE_CRITICAL_LOW);
        settings.setHeartRateCriticalHigh(DEFAULT_HEART_RATE_CRITICAL_HIGH);
        settings.setBmiLow(DEFAULT_BMI_LOW);
        settings.setBmiHigh(DEFAULT_BMI_HIGH);
        settings.setBmiObesity(DEFAULT_BMI_OBESITY);
        return settings;
    }

    private WarningThresholdSettingsVo mergeWithDefaults(WarningThresholdSettingsVo source)
    {
        WarningThresholdSettingsVo defaults = createDefaultSettings();
        WarningThresholdSettingsVo merged = new WarningThresholdSettingsVo();
        merged.setBpSysLow(source != null && source.getBpSysLow() != null ? source.getBpSysLow() : defaults.getBpSysLow());
        merged.setBpSysHigh(source != null && source.getBpSysHigh() != null ? source.getBpSysHigh() : defaults.getBpSysHigh());
        merged.setBpSysCritical(source != null && source.getBpSysCritical() != null ? source.getBpSysCritical() : defaults.getBpSysCritical());
        merged.setBpDiaLow(source != null && source.getBpDiaLow() != null ? source.getBpDiaLow() : defaults.getBpDiaLow());
        merged.setBpDiaHigh(source != null && source.getBpDiaHigh() != null ? source.getBpDiaHigh() : defaults.getBpDiaHigh());
        merged.setBpDiaCritical(source != null && source.getBpDiaCritical() != null ? source.getBpDiaCritical() : defaults.getBpDiaCritical());
        merged.setBloodSugarLow(source != null && source.getBloodSugarLow() != null ? source.getBloodSugarLow() : defaults.getBloodSugarLow());
        merged.setBloodSugarHigh(source != null && source.getBloodSugarHigh() != null ? source.getBloodSugarHigh() : defaults.getBloodSugarHigh());
        merged.setBloodSugarCriticalLow(source != null && source.getBloodSugarCriticalLow() != null ? source.getBloodSugarCriticalLow() : defaults.getBloodSugarCriticalLow());
        merged.setBloodSugarCriticalHigh(source != null && source.getBloodSugarCriticalHigh() != null ? source.getBloodSugarCriticalHigh() : defaults.getBloodSugarCriticalHigh());
        merged.setHeartRateLow(source != null && source.getHeartRateLow() != null ? source.getHeartRateLow() : defaults.getHeartRateLow());
        merged.setHeartRateHigh(source != null && source.getHeartRateHigh() != null ? source.getHeartRateHigh() : defaults.getHeartRateHigh());
        merged.setHeartRateCriticalLow(source != null && source.getHeartRateCriticalLow() != null ? source.getHeartRateCriticalLow() : defaults.getHeartRateCriticalLow());
        merged.setHeartRateCriticalHigh(source != null && source.getHeartRateCriticalHigh() != null ? source.getHeartRateCriticalHigh() : defaults.getHeartRateCriticalHigh());
        merged.setBmiLow(source != null && source.getBmiLow() != null ? source.getBmiLow() : defaults.getBmiLow());
        merged.setBmiHigh(source != null && source.getBmiHigh() != null ? source.getBmiHigh() : defaults.getBmiHigh());
        merged.setBmiObesity(source != null && source.getBmiObesity() != null ? source.getBmiObesity() : defaults.getBmiObesity());
        return merged;
    }

    private Map<String, String> buildThresholdTexts(WarningThresholdSettingsVo settings)
    {
        Map<String, String> texts = new LinkedHashMap<>();
        texts.put("血压异常", String.format("收缩压 %d-%d mmHg，舒张压 %d-%d mmHg", settings.getBpSysLow(), settings.getBpSysHigh(), settings.getBpDiaLow(), settings.getBpDiaHigh()));
        texts.put("血糖异常", String.format("空腹血糖 %.1f-%.1f mmol/L", settings.getBloodSugarLow(), settings.getBloodSugarHigh()));
        texts.put("心率异常", String.format("心率 %d-%d bpm", settings.getHeartRateLow(), settings.getHeartRateHigh()));
        texts.put("体重超标", String.format("BMI %.1f-%.1f，>=%.1f 重点干预", settings.getBmiLow(), settings.getBmiHigh(), settings.getBmiObesity()));
        texts.put("体重过轻", String.format("BMI %.1f-%.1f，<%.1f 需关注", settings.getBmiLow(), settings.getBmiHigh(), settings.getBmiLow()));
        return texts;
    }

    private void validateSettings(WarningThresholdSettingsVo settings)
    {
        if (settings.getBpSysLow() >= settings.getBpSysHigh() || settings.getBpSysHigh() >= settings.getBpSysCritical())
        {
            throw new ServiceException("血压收缩压阈值必须满足 下限 < 上限 < 高危阈值");
        }
        if (settings.getBpDiaLow() >= settings.getBpDiaHigh() || settings.getBpDiaHigh() >= settings.getBpDiaCritical())
        {
            throw new ServiceException("血压舒张压阈值必须满足 下限 < 上限 < 高危阈值");
        }
        if (settings.getBloodSugarCriticalLow() >= settings.getBloodSugarLow()
            || settings.getBloodSugarLow() >= settings.getBloodSugarHigh()
            || settings.getBloodSugarHigh() >= settings.getBloodSugarCriticalHigh())
        {
            throw new ServiceException("血糖阈值必须满足 高危下限 < 正常下限 < 正常上限 < 高危上限");
        }
        if (settings.getHeartRateCriticalLow() >= settings.getHeartRateLow()
            || settings.getHeartRateLow() >= settings.getHeartRateHigh()
            || settings.getHeartRateHigh() >= settings.getHeartRateCriticalHigh())
        {
            throw new ServiceException("心率阈值必须满足 高危下限 < 正常下限 < 正常上限 < 高危上限");
        }
        if (settings.getBmiLow() >= settings.getBmiHigh() || settings.getBmiHigh() >= settings.getBmiObesity())
        {
            throw new ServiceException("BMI 阈值必须满足 下限 < 正常上限 < 肥胖阈值");
        }
    }

    private void saveConfig(String key, String name, String value, String operator)
    {
        SysConfig existing = configMapper.checkConfigKeyUnique(key);
        if (existing == null)
        {
            SysConfig config = new SysConfig();
            config.setConfigName(name);
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType("N");
            config.setCreateBy(operator);
            configService.insertConfig(config);
            return;
        }

        SysConfig config = new SysConfig();
        config.setConfigId(existing.getConfigId());
        config.setConfigName(name);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigType(existing.getConfigType());
        config.setRemark(existing.getRemark());
        config.setUpdateBy(operator);
        configService.updateConfig(config);
    }

    private Integer parseInteger(String value)
    {
        if (isBlank(value))
        {
            return null;
        }
        return Integer.valueOf(value.trim());
    }

    private Double parseDouble(String value)
    {
        if (isBlank(value))
        {
            return null;
        }
        return Double.valueOf(value.trim());
    }

    private String toValue(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
