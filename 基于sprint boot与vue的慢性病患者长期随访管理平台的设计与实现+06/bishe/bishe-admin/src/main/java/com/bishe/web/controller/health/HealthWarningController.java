package com.bishe.web.controller.health;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.bishe.common.utils.SecurityUtils;
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
import com.bishe.common.annotation.Log;
import com.bishe.common.core.controller.BaseController;
import com.bishe.common.core.domain.AjaxResult;
import com.bishe.common.core.page.TableDataInfo;
import com.bishe.common.enums.BusinessType;
import com.bishe.common.utils.poi.ExcelUtil;
import com.bishe.system.domain.HealthWarning;
import com.bishe.system.service.IHealthWarningService;

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

        /**
         * 获取健康预警列表
         */
        @PreAuthorize("@ss.hasPermi('health:warning:list') || isAuthenticated()")
        @GetMapping("/list")
        public TableDataInfo list(HealthWarning healthWarning)
        {
            startPage();
            if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin")) {
                if (SecurityUtils.hasRole("doctor")) {
                    healthWarning.getParams().put("doctorId", SecurityUtils.getUserId());
                } else {
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
            if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin") && !SecurityUtils.hasRole("doctor")) {
                healthWarning.setUserId(SecurityUtils.getUserId());
            }
            List<HealthWarning> list = healthWarningService.selectHealthWarningList(healthWarning);
            ExcelUtil<HealthWarning> util = new ExcelUtil<HealthWarning>(HealthWarning.class);
            util.exportExcel(response, list, "健康预警数据");
        }
    /**
     * 根据健康预警编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('health:warning:query')")
    @GetMapping(value = "/{warningId}")
    public AjaxResult getInfo(@PathVariable Long warningId)
    {
        return success(healthWarningService.selectHealthWarningById(warningId));
    }

    /**
     * 新增健康预警
     */
    @PreAuthorize("@ss.hasPermi('health:warning:add')")
    @Log(title = "健康预警", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HealthWarning healthWarning)
    {
        return toAjax(healthWarningService.insertHealthWarning(healthWarning));
    }

    /**
     * 修改健康预警
     */
    @PreAuthorize("@ss.hasPermi('health:warning:edit')")
    @Log(title = "健康预警", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HealthWarning healthWarning)
    {
        return toAjax(healthWarningService.updateHealthWarning(healthWarning));
    }

    /**
     * 标记预警为已读 (患者专用)
     */
    @Log(title = "健康预警", businessType = BusinessType.UPDATE)
    @PutMapping("/read")
    public AjaxResult markRead(@RequestBody HealthWarning healthWarning)
    {
        // 1. Get existing warning
        HealthWarning existing = healthWarningService.selectHealthWarningById(healthWarning.getWarningId());
        if (existing == null) {
            return error("预警不存在");
        }
        
        // 2. Check permission: Must be Admin, Doctor, or the Patient themselves
        Long userId = SecurityUtils.getUserId();
        boolean isSelf = existing.getUserId().equals(userId);
        boolean canEdit = SecurityUtils.isAdmin() || SecurityUtils.hasRole("admin") || SecurityUtils.hasRole("doctor");
        
        if (!isSelf && !canEdit) {
            return error("无权操作此预警");
        }

        // 3. Only update status
        existing.setIsRead("1");
        return toAjax(healthWarningService.updateHealthWarning(existing));
    }

    /**
     * 删除健康预警
     */
    @PreAuthorize("@ss.hasPermi('health:warning:remove')")
    @Log(title = "健康预警", businessType = BusinessType.DELETE)
    @DeleteMapping("/{warningIds}")
    public AjaxResult remove(@PathVariable Long[] warningIds)
    {
        return toAjax(healthWarningService.deleteHealthWarningByIds(warningIds));
    }
}
