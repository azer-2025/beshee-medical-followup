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
import com.bishe.system.domain.HealthData;
import com.bishe.system.service.IHealthDataService;

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

            /**
             * 获取健康数据采集列表
             */
                @PreAuthorize("@ss.hasPermi('health:data:list') || isAuthenticated()")
                @GetMapping("/list")
                public TableDataInfo list(HealthData healthData)
                {
                    startPage();
                    if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin")) {
                        if (SecurityUtils.hasRole("doctor")) {
                            healthData.getParams().put("doctorId", SecurityUtils.getUserId());
                        } else {
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
                if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin") && !SecurityUtils.hasRole("doctor")) {
                    healthData.setUserId(SecurityUtils.getUserId());
                }
                List<HealthData> list = healthDataService.selectHealthDataList(healthData);
                ExcelUtil<HealthData> util = new ExcelUtil<HealthData>(HealthData.class);
                util.exportExcel(response, list, "健康数据采集数据");
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
                    if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin") && !SecurityUtils.hasRole("doctor")) {
                        healthData.setUserId(SecurityUtils.getUserId());
                    }
                    return toAjax(healthDataService.insertHealthData(healthData));
                }        /**
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
        }}
