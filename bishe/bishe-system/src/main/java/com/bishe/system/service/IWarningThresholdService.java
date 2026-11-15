package com.bishe.system.service;

import java.util.Map;
import com.bishe.system.domain.vo.WarningThresholdSettingsVo;

public interface IWarningThresholdService
{
    WarningThresholdSettingsVo getStoredSettings();

    WarningThresholdSettingsVo getEffectiveSettings();

    Map<String, String> getDefaultThresholdTexts();

    Map<String, String> getEffectiveThresholdTexts();

    void saveSettings(WarningThresholdSettingsVo settings, String operator);
}
