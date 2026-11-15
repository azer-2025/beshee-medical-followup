package com.bishe.web.controller.followup;

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
import com.bishe.system.domain.FollowupPlan;
import com.bishe.system.service.IFollowupPlanService;
import com.bishe.web.service.followup.FollowupPlanScheduleService;

/**
 * 随访计划操作处理
 *
 * @author bishe
 */
@RestController
@RequestMapping("/followup/plan")
public class FollowupPlanController extends BaseController
{
    @Autowired
    private IFollowupPlanService followupPlanService;

    @Autowired
    private FollowupPlanScheduleService followupPlanScheduleService;

        /**
         * 获取随访计划列表
         */
        @PreAuthorize("@ss.hasPermi('followup:plan:list') || isAuthenticated()")
        @GetMapping("/list")
        public TableDataInfo list(FollowupPlan followupPlan)
        {
            startPage();
            if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin")) {
                if (SecurityUtils.hasRole("doctor")) {
                    followupPlan.setDoctorId(SecurityUtils.getUserId());
                } else {
                    followupPlan.setPatientId(SecurityUtils.getUserId());
                }
            }
            List<FollowupPlan> list = followupPlanService.selectFollowupPlanList(followupPlan);
            return getDataTable(list);
        }
        
        @Log(title = "随访计划", businessType = BusinessType.EXPORT)
        @PreAuthorize("@ss.hasPermi('followup:plan:export') || isAuthenticated()")
        @PostMapping("/export")
        public void export(HttpServletResponse response, FollowupPlan followupPlan)
        {
            if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin")) {
                if (SecurityUtils.hasRole("doctor")) {
                    followupPlan.setDoctorId(SecurityUtils.getUserId());
                } else {
                    followupPlan.setPatientId(SecurityUtils.getUserId());
                }
            }
            List<FollowupPlan> list = followupPlanService.selectFollowupPlanList(followupPlan);
            ExcelUtil<FollowupPlan> util = new ExcelUtil<FollowupPlan>(FollowupPlan.class);
            util.exportExcel(response, list, "随访计划数据");
        }
    
        /**
         * 根据随访计划编号获取详细信息
         */
        @PreAuthorize("@ss.hasPermi('followup:plan:query') || isAuthenticated()")
        @GetMapping(value = "/{planId}")
        public AjaxResult getInfo(@PathVariable Long planId)
        {
            return success(followupPlanService.selectFollowupPlanById(planId));
        }
    
        /**
         * 新增随访计划
         */
        @PreAuthorize("@ss.hasPermi('followup:plan:add') || isAuthenticated()")
        @Log(title = "随访计划", businessType = BusinessType.INSERT)
        @PostMapping
        public AjaxResult add(@RequestBody FollowupPlan followupPlan)
        {
            if (SecurityUtils.hasRole("doctor")) {
                followupPlan.setDoctorId(SecurityUtils.getUserId());
            }
            followupPlan.setCreateBy(getUsername());
            return toAjax(followupPlanScheduleService.insertFollowupPlan(followupPlan, getUsername()));
        }
    /**
     * 修改随访计划
     */
    @PreAuthorize("@ss.hasPermi('followup:plan:edit') || isAuthenticated()")
    @Log(title = "随访计划", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FollowupPlan followupPlan)
    {
        followupPlan.setUpdateBy(getUsername());
        return toAjax(followupPlanScheduleService.updateFollowupPlan(followupPlan, getUsername()));
    }

    /**
     * 删除随访计划
     */
    @PreAuthorize("@ss.hasPermi('followup:plan:remove') || isAuthenticated()")
    @Log(title = "随访计划", businessType = BusinessType.DELETE)
    @DeleteMapping("/{planIds}")
    public AjaxResult remove(@PathVariable Long[] planIds)
    {
        return toAjax(followupPlanScheduleService.deleteFollowupPlanByIds(planIds));
    }
}
