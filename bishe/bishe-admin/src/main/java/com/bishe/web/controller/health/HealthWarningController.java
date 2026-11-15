package com.bishe.web.controller.health;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import com.bishe.common.annotation.Log;
import com.bishe.common.core.controller.BaseController;
import com.bishe.common.core.domain.AjaxResult;
import com.bishe.common.core.domain.entity.SysUser;
import com.bishe.common.core.page.TableDataInfo;
import com.bishe.common.enums.BusinessType;
import com.bishe.common.utils.DateUtils;
import com.bishe.common.utils.SecurityUtils;
import com.bishe.common.utils.StringUtils;
import com.bishe.common.utils.poi.ExcelUtil;
import com.bishe.system.domain.HealthWarning;
import com.bishe.system.domain.vo.WarningThresholdSettingsVo;
import com.bishe.system.service.IDoctorPatientService;
import com.bishe.system.service.IHealthWarningService;
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
 * 健康预警操作处理
 *
 * @author bishe
 */
@RestController
@RequestMapping("/health/warning")
public class HealthWarningController extends BaseController
{
    @Autowired
    private IHealthWarningService healthWarningService;

    @Autowired
    private IDoctorPatientService doctorPatientService;

    @Autowired
    private IWarningThresholdService warningThresholdService;

    /**
     * 获取健康预警列表
     */
    @PreAuthorize("@ss.hasPermi('health:warning:list') || isAuthenticated()")
    @GetMapping("/list")
    public TableDataInfo list(HealthWarning healthWarning)
    {
        startPage();
        if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin"))
        {
            if (SecurityUtils.hasRole("doctor"))
            {
                healthWarning.getParams().put("doctorId", SecurityUtils.getUserId());
            }
            else
            {
                healthWarning.setUserId(SecurityUtils.getUserId());
            }
        }
        List<HealthWarning> list = healthWarningService.selectHealthWarningList(healthWarning);
        return getDataTable(list);
    }

    @Log(title = "健康预警", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('health:warning:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HealthWarning healthWarning)
    {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin"))
        {
            if (SecurityUtils.hasRole("doctor"))
            {
                healthWarning.getParams().put("doctorId", SecurityUtils.getUserId());
            }
            else
            {
                healthWarning.setUserId(SecurityUtils.getUserId());
            }
        }
        List<HealthWarning> list = healthWarningService.selectHealthWarningList(healthWarning);
        ExcelUtil<HealthWarning> util = new ExcelUtil<>(HealthWarning.class);
        util.exportExcel(response, list, "健康预警数据");
    }

    /**
     * 根据健康预警编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:warning:query') || isAuthenticated()")
    @GetMapping(value = "/{warningId}")
    public AjaxResult getInfo(@PathVariable Long warningId)
    {
        HealthWarning healthWarning = healthWarningService.selectHealthWarningById(warningId);
        if (healthWarning == null)
        {
            return error("预警不存在");
        }
        if (!canViewWarning(healthWarning))
        {
            return error("无权查看该预警");
        }
        return success(healthWarning);
    }

    /**
     * 新增健康预警
     */
    @PreAuthorize("@ss.hasPermi('health:warning:add') || isAuthenticated()")
    @Log(title = "健康预警", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HealthWarning healthWarning)
    {
        if (!canCreateWarning(healthWarning))
        {
            return error("当前用户无权新增该预警");
        }
        if (StringUtils.isEmpty(healthWarning.getConfirmStatus()))
        {
            healthWarning.setConfirmStatus("0");
        }
        return toAjax(healthWarningService.insertHealthWarning(healthWarning));
    }

    /**
     * 修改健康预警
     */
    @PreAuthorize("@ss.hasPermi('health:warning:edit') || isAuthenticated()")
    @Log(title = "健康预警", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HealthWarning healthWarning)
    {
        HealthWarning existing = healthWarningService.selectHealthWarningById(healthWarning.getWarningId());
        if (existing == null)
        {
            return error("预警不存在");
        }
        if (!canEditWarning(existing))
        {
            return error("当前用户无权修改该预警");
        }

        healthWarning.setUserId(existing.getUserId());
        healthWarning.setCreateTime(existing.getCreateTime());
        if (StringUtils.isEmpty(healthWarning.getThresholdRange()))
        {
            healthWarning.setThresholdRange(existing.getThresholdRange());
        }
        if (StringUtils.isEmpty(healthWarning.getIsRead()))
        {
            healthWarning.setIsRead(existing.getIsRead());
        }

        if (!hasManagePermission("health:warning:edit") && isDoctor())
        {
            healthWarning.setConfirmStatus("1");
            healthWarning.setConfirmBy(getUsername());
            healthWarning.setConfirmTime(DateUtils.getNowDate());
            if (StringUtils.isEmpty(healthWarning.getConfirmNote()))
            {
                healthWarning.setConfirmNote("医生已完成二次复核确认");
            }
            if (StringUtils.isEmpty(healthWarning.getWarningMsg()))
            {
                healthWarning.setWarningMsg(existing.getWarningMsg());
            }
        }
        else
        {
            if (StringUtils.isEmpty(healthWarning.getConfirmStatus()))
            {
                healthWarning.setConfirmStatus(existing.getConfirmStatus());
            }
            if (StringUtils.isEmpty(healthWarning.getConfirmNote()))
            {
                healthWarning.setConfirmNote(existing.getConfirmNote());
            }
            if (StringUtils.isEmpty(healthWarning.getConfirmBy()))
            {
                healthWarning.setConfirmBy(existing.getConfirmBy());
            }
            if (healthWarning.getConfirmTime() == null)
            {
                healthWarning.setConfirmTime(existing.getConfirmTime());
            }
        }
        return toAjax(healthWarningService.updateHealthWarning(healthWarning));
    }

    /**
     * 标记预警为已读
     */
    @Log(title = "健康预警", businessType = BusinessType.UPDATE)
    @PutMapping("/read")
    public AjaxResult markRead(@RequestBody HealthWarning healthWarning)
    {
        HealthWarning existing = healthWarningService.selectHealthWarningById(healthWarning.getWarningId());
        if (existing == null)
        {
            return error("预警不存在");
        }

        Long userId = SecurityUtils.getUserId();
        boolean isSelf = existing.getUserId().equals(userId);
        boolean canEdit = SecurityUtils.isAdmin() || SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("doctor");

        if (!isSelf && !canEdit)
        {
            return error("无权操作此预警");
        }

        existing.setIsRead("1");
        return toAjax(healthWarningService.updateHealthWarning(existing));
    }

    /**
     * 删除健康预警
     */
    @PreAuthorize("@ss.hasPermi('health:warning:remove') || isAuthenticated()")
    @Log(title = "健康预警", businessType = BusinessType.DELETE)
    @DeleteMapping("/{warningIds}")
    public AjaxResult remove(@PathVariable Long[] warningIds)
    {
        if (!canRemoveWarnings(warningIds))
        {
            return error("当前用户无权删除所选预警");
        }
        return toAjax(healthWarningService.deleteHealthWarningByIds(warningIds));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/thresholds")
    public AjaxResult getThresholdSettings()
    {
        Map<String, Object> data = new HashMap<>(4);
        data.put("settings", warningThresholdService.getStoredSettings());
        data.put("defaultTexts", warningThresholdService.getDefaultThresholdTexts());
        data.put("effectiveTexts", warningThresholdService.getEffectiveThresholdTexts());
        data.put("canEdit", isAdminUser() || isDoctor());
        return success(data);
    }

    @PreAuthorize("@ss.hasRole('doctor') or @ss.hasRole('admin')")
    @PutMapping("/thresholds")
    public AjaxResult updateThresholdSettings(@RequestBody WarningThresholdSettingsVo settings)
    {
        warningThresholdService.saveSettings(settings, getUsername());
        return success();
    }

    private boolean canViewWarning(HealthWarning healthWarning)
    {
        if (healthWarning == null)
        {
            return false;
        }
        if (hasManagePermission("health:warning:query"))
        {
            return true;
        }
        Long currentUserId = SecurityUtils.getUserId();
        return healthWarning.getUserId() != null
            && (healthWarning.getUserId().equals(currentUserId) || isDoctorPatient(healthWarning.getUserId()));
    }

    private boolean canCreateWarning(HealthWarning healthWarning)
    {
        if (healthWarning == null || healthWarning.getUserId() == null)
        {
            return false;
        }
        if (hasManagePermission("health:warning:add"))
        {
            return true;
        }
        return isDoctorPatient(healthWarning.getUserId());
    }

    private boolean canEditWarning(HealthWarning healthWarning)
    {
        if (healthWarning == null)
        {
            return false;
        }
        if (hasManagePermission("health:warning:edit"))
        {
            return true;
        }
        return isDoctorPatient(healthWarning.getUserId());
    }

    private boolean canRemoveWarnings(Long[] warningIds)
    {
        if (warningIds == null || warningIds.length == 0)
        {
            return false;
        }
        if (hasManagePermission("health:warning:remove"))
        {
            return true;
        }
        if (!isDoctor())
        {
            return false;
        }
        for (Long warningId : warningIds)
        {
            HealthWarning healthWarning = healthWarningService.selectHealthWarningById(warningId);
            if (healthWarning == null || !isDoctorPatient(healthWarning.getUserId()))
            {
                return false;
            }
        }
        return true;
    }

    private boolean hasManagePermission(String permission)
    {
        return isAdminUser() || SecurityUtils.hasPermi(permission);
    }

    private boolean isAdminUser()
    {
        return SecurityUtils.isAdmin() || SecurityUtils.hasRole("admin");
    }

    private boolean isDoctor()
    {
        return SecurityUtils.hasRole("doctor");
    }

    private boolean isDoctorPatient(Long patientId)
    {
        if (!isDoctor() || patientId == null)
        {
            return false;
        }
        Long doctorId = SecurityUtils.getUserId();
        for (SysUser doctor : doctorPatientService.selectDoctorByPatientId(patientId))
        {
            if (doctorId.equals(doctor.getUserId()))
            {
                return true;
            }
        }
        return false;
    }
}
