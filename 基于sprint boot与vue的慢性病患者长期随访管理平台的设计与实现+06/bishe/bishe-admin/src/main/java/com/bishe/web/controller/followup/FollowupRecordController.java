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
import com.bishe.system.domain.FollowupRecord;
import com.bishe.system.service.IFollowupRecordService;

/**
 * 随访记录操作处理
 *
 * @author bishe
 */
@RestController
@RequestMapping("/followup/record")
public class FollowupRecordController extends BaseController
{
    @Autowired
    private IFollowupRecordService followupRecordService;

        /**
         * 获取随访记录列表
         */
        @PreAuthorize("@ss.hasPermi('followup:record:list') || isAuthenticated()")
        @GetMapping("/list")
        public TableDataInfo list(FollowupRecord followupRecord)
        {
            startPage();
            if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin")) {
                if (SecurityUtils.hasRole("doctor")) {
                    followupRecord.setDoctorId(SecurityUtils.getUserId());
                } else {
                    followupRecord.setPatientId(SecurityUtils.getUserId());
                }
            }
            List<FollowupRecord> list = followupRecordService.selectFollowupRecordList(followupRecord);
            return getDataTable(list);
        }
        
        @Log(title = "随访记录", businessType = BusinessType.EXPORT)
        @PreAuthorize("@ss.hasPermi('followup:record:export') || isAuthenticated()")
        @PostMapping("/export")
        public void export(HttpServletResponse response, FollowupRecord followupRecord)
        {
            if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin")) {
                if (SecurityUtils.hasRole("doctor")) {
                    followupRecord.setDoctorId(SecurityUtils.getUserId());
                } else {
                    followupRecord.setPatientId(SecurityUtils.getUserId());
                }
            }
            List<FollowupRecord> list = followupRecordService.selectFollowupRecordList(followupRecord);
            ExcelUtil<FollowupRecord> util = new ExcelUtil<FollowupRecord>(FollowupRecord.class);
            util.exportExcel(response, list, "随访记录数据");
        }
    
        /**
         * 根据随访记录编号获取详细信息
         */
        @PreAuthorize("@ss.hasPermi('followup:record:query') || isAuthenticated()")
        @GetMapping(value = "/{recordId}")
        public AjaxResult getInfo(@PathVariable Long recordId)
        {
            return success(followupRecordService.selectFollowupRecordById(recordId));
        }
    
        /**
         * 新增随访记录
         */
        @PreAuthorize("@ss.hasPermi('followup:record:add') || isAuthenticated()")
        @Log(title = "随访记录", businessType = BusinessType.INSERT)
        @PostMapping
        public AjaxResult add(@RequestBody FollowupRecord followupRecord)
        {
            if (SecurityUtils.hasRole("doctor")) {
                followupRecord.setDoctorId(SecurityUtils.getUserId());
            } else if (!SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin")) {
                followupRecord.setPatientId(SecurityUtils.getUserId());
            }
            return toAjax(followupRecordService.insertFollowupRecord(followupRecord));
        }
    /**
     * 修改随访记录
     */
    @PreAuthorize("@ss.hasPermi('followup:record:edit') || isAuthenticated()")
    @Log(title = "随访记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FollowupRecord followupRecord)
    {
        return toAjax(followupRecordService.updateFollowupRecord(followupRecord));
    }

    /**
     * 删除随访记录
     */
    @PreAuthorize("@ss.hasPermi('followup:record:remove') || isAuthenticated()")
    @Log(title = "随访记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(followupRecordService.deleteFollowupRecordByIds(recordIds));
    }
}
