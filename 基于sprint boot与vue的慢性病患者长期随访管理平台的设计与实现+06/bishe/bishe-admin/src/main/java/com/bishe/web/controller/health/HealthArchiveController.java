package com.bishe.web.controller.health;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.bishe.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
import com.bishe.system.domain.HealthArchive;
import com.bishe.system.service.IHealthArchiveService;

/**
 * 电子健康档案操作处理
 *
 * @author bishe
 */
@RestController
@RequestMapping("/health/archive")
public class HealthArchiveController extends BaseController
{
    @Autowired
    private IHealthArchiveService healthArchiveService;

                @PreAuthorize("@ss.hasPermi('health:archive:list') || isAuthenticated()")
                @GetMapping("/list")
                public TableDataInfo list(HealthArchive healthArchive)
                {
                    startPage();
                    if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin")) {
                        if (SecurityUtils.hasRole("doctor")) {
                            healthArchive.getParams().put("doctorId", SecurityUtils.getUserId());
                        } else {
                            healthArchive.setUserId(SecurityUtils.getUserId());
                        }
                    }
                    List<HealthArchive> list = healthArchiveService.selectHealthArchiveList(healthArchive);
                    return getDataTable(list);
                }    @Log(title = "电子健康档案", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('health:archive:export') || isAuthenticated()")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HealthArchive healthArchive)
    {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin") && !SecurityUtils.hasRole("doctor")) {
            healthArchive.setUserId(SecurityUtils.getUserId());
        }
        List<HealthArchive> list = healthArchiveService.selectHealthArchiveList(healthArchive);
        ExcelUtil<HealthArchive> util = new ExcelUtil<HealthArchive>(HealthArchive.class);
        util.exportExcel(response, list, "电子健康档案数据");
    }    
                /**
                 * 根据电子健康档案编号获取详细信息
                 */
                @PreAuthorize("@ss.hasPermi('health:archive:query') || isAuthenticated()")
                @GetMapping(value = "/{archiveId}")
                public AjaxResult getInfo(@PathVariable Long archiveId)
                {
                    return success(healthArchiveService.selectHealthArchiveById(archiveId));
                }    /**
     * 新增电子健康档案
     */
    @PreAuthorize("@ss.hasPermi('health:archive:add') || isAuthenticated()")
    @Log(title = "电子健康档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HealthArchive healthArchive)
    {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin") && !SecurityUtils.hasRole("doctor")) {
            healthArchive.setUserId(SecurityUtils.getUserId());
        }
        healthArchive.setCreateBy(getUsername());
        return toAjax(healthArchiveService.insertHealthArchive(healthArchive));
    }    /**
     * 修改电子健康档案
     */
    @PreAuthorize("@ss.hasPermi('health:archive:edit') || isAuthenticated()")
    @Log(title = "电子健康档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HealthArchive healthArchive)
    {
        healthArchive.setUpdateBy(getUsername());
        return toAjax(healthArchiveService.updateHealthArchive(healthArchive));
    }

    /**
     * 删除电子健康档案
     */
    @PreAuthorize("@ss.hasPermi('health:archive:remove') || isAuthenticated()")
    @Log(title = "电子健康档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{archiveIds}")
    public AjaxResult remove(@PathVariable Long[] archiveIds)
    {
        return toAjax(healthArchiveService.deleteHealthArchiveByIds(archiveIds));
    }
}
