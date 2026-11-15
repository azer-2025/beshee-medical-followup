package com.bishe.web.controller.health;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.bishe.common.annotation.Log;
import com.bishe.common.core.controller.BaseController;
import com.bishe.common.core.domain.AjaxResult;
import com.bishe.common.core.page.TableDataInfo;
import com.bishe.common.enums.BusinessType;
import com.bishe.common.utils.SecurityUtils;
import com.bishe.common.utils.poi.ExcelUtil;
import com.bishe.system.domain.HealthData;
import com.bishe.system.domain.vo.WarningThresholdSettingsVo;
import com.bishe.system.service.IHealthDataService;
import com.bishe.system.service.IWarningThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康数据采集操作处理
 *
 * @author bishe
 */
@RestController
@RequestMapping("/health/data")
public class HealthDataController extends BaseController
{
    @Autowired
    private IHealthDataService healthDataService;

    @Autowired
    private IWarningThresholdService warningThresholdService;

    /**
     * 获取健康数据采集列表
     */
    @PreAuthorize("@ss.hasPermi('health:data:list') || isAuthenticated()")
    @GetMapping("/list")
    public TableDataInfo list(HealthData healthData)
    {
        startPage();
        if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin"))
        {
            if (SecurityUtils.hasRole("doctor"))
            {
                healthData.getParams().put("doctorId", SecurityUtils.getUserId());
            }
            else
            {
                healthData.setUserId(SecurityUtils.getUserId());
            }
        }
        List<HealthData> list = healthDataService.selectHealthDataList(healthData);
        return getDataTable(list);
    }

    @Log(title = "健康数据采集", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('health:data:export') || isAuthenticated()")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HealthData healthData)
    {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin"))
        {
            if (SecurityUtils.hasRole("doctor"))
            {
                healthData.getParams().put("doctorId", SecurityUtils.getUserId());
            }
            else
            {
                healthData.setUserId(SecurityUtils.getUserId());
            }
        }
        List<HealthData> list = healthDataService.selectHealthDataList(healthData);
        enrichExportRows(list);
        ExcelUtil<HealthData> util = new ExcelUtil<>(HealthData.class);
        util.exportExcel(response, list, "体检报告");
    }

    /**
     * 根据健康数据采集编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:data:query') || isAuthenticated()")
    @GetMapping(value = "/{dataId}")
    public AjaxResult getInfo(@PathVariable Long dataId)
    {
        return success(healthDataService.selectHealthDataById(dataId));
    }

    /**
     * 新增健康数据采集
     */
    @PreAuthorize("@ss.hasPermi('health:data:add') || isAuthenticated()")
    @Log(title = "健康数据采集", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HealthData healthData)
    {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin") && !SecurityUtils.hasRole("doctor"))
        {
            healthData.setUserId(SecurityUtils.getUserId());
        }
        return toAjax(healthDataService.insertHealthData(healthData));
    }

    /**
     * 修改健康数据采集
     */
    @PreAuthorize("@ss.hasPermi('health:data:edit') || isAuthenticated()")
    @Log(title = "健康数据采集", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HealthData healthData)
    {
        return toAjax(healthDataService.updateHealthData(healthData));
    }

    /**
     * 删除健康数据采集
     */
    @PreAuthorize("@ss.hasPermi('health:data:remove') || isAuthenticated()")
    @Log(title = "健康数据采集", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dataIds}")
    public AjaxResult remove(@PathVariable Long[] dataIds)
    {
        return toAjax(healthDataService.deleteHealthDataByIds(dataIds));
    }

    private void enrichExportRows(List<HealthData> list)
    {
        WarningThresholdSettingsVo thresholds = warningThresholdService.getEffectiveSettings();
        for (HealthData item : list)
        {
            item.setBloodPressureReference(String.format("收缩压 %d-%d mmHg；舒张压 %d-%d mmHg",
                thresholds.getBpSysLow(), thresholds.getBpSysHigh(), thresholds.getBpDiaLow(), thresholds.getBpDiaHigh()));
            item.setHeartRateReference(String.format("%d-%d bpm", thresholds.getHeartRateLow(), thresholds.getHeartRateHigh()));
            item.setBloodSugarReference(String.format("%.1f-%.1f mmol/L", thresholds.getBloodSugarLow(), thresholds.getBloodSugarHigh()));
            item.setBmiReference(String.format("%.1f-%.1f", thresholds.getBmiLow(), thresholds.getBmiHigh()));

            StringBuilder abnormalItems = new StringBuilder();
            if (item.getHeight() != null && item.getHeight() > 0 && item.getWeight() != null)
            {
                double heightM = item.getHeight() / 100.0;
                double bmi = item.getWeight() / (heightM * heightM);
                item.setBmiValue(String.format("%.1f", bmi));

                double minWeight = thresholds.getBmiLow() * heightM * heightM;
                double maxWeight = thresholds.getBmiHigh() * heightM * heightM;
                item.setWeightReference(String.format("%.1f-%.1f kg", minWeight, maxWeight));

                if (bmi < thresholds.getBmiLow())
                {
                    appendAbnormal(abnormalItems, String.format("BMI %.1f 偏低", bmi));
                }
                else if (bmi >= thresholds.getBmiObesity())
                {
                    appendAbnormal(abnormalItems, String.format("BMI %.1f 达到肥胖预警", bmi));
                }
                else if (bmi > thresholds.getBmiHigh())
                {
                    appendAbnormal(abnormalItems, String.format("BMI %.1f 偏高", bmi));
                }
            }
            else
            {
                item.setBmiValue("");
                item.setWeightReference("请结合身高评估");
            }

            if (item.getBloodPressureSys() != null && item.getBloodPressureDia() != null)
            {
                if (item.getBloodPressureSys() < thresholds.getBpSysLow() || item.getBloodPressureSys() > thresholds.getBpSysHigh()
                    || item.getBloodPressureDia() < thresholds.getBpDiaLow() || item.getBloodPressureDia() > thresholds.getBpDiaHigh())
                {
                    appendAbnormal(abnormalItems, "血压超出参考范围");
                }
            }
            if (item.getHeartRate() != null
                && (item.getHeartRate() < thresholds.getHeartRateLow() || item.getHeartRate() > thresholds.getHeartRateHigh()))
            {
                appendAbnormal(abnormalItems, "心率超出参考范围");
            }
            if (item.getBloodSugar() != null
                && (item.getBloodSugar() < thresholds.getBloodSugarLow() || item.getBloodSugar() > thresholds.getBloodSugarHigh()))
            {
                appendAbnormal(abnormalItems, "血糖超出参考范围");
            }

            item.setAbnormalItems(abnormalItems.length() == 0 ? "未见明显异常" : abnormalItems.toString());
            item.setHealthAssessment(abnormalItems.length() == 0 ? "本次体检指标整体在参考范围内" : "本次体检存在异常指标，建议结合医生意见进一步复核");
        }
    }

    private void appendAbnormal(StringBuilder abnormalItems, String item)
    {
        if (abnormalItems.length() > 0)
        {
            abnormalItems.append("；");
        }
        abnormalItems.append(item);
    }
}
