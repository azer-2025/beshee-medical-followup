package com.bishe.web.controller.monitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bishe.common.core.controller.BaseController;
import com.bishe.common.core.domain.AjaxResult;
import com.bishe.common.core.domain.entity.SysUser;
import com.bishe.common.utils.DateUtils;
import com.bishe.common.utils.SecurityUtils;
import com.bishe.system.domain.FollowupRecord;
import com.bishe.system.domain.HealthWarning;
import com.bishe.system.service.IDoctorPatientService;
import com.bishe.system.service.IFollowupRecordService;
import com.bishe.system.service.IHealthWarningService;
import com.bishe.system.service.ISysUserService;

/**
 * 首页数据分析
 * 
 * @author bishe
 */
@RestController
@RequestMapping("/index")
public class DashboardController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private IHealthWarningService warningService;

    @Autowired
    private IFollowupRecordService recordService;

    @Autowired
    private IDoctorPatientService doctorPatientService;

    /**
     * 获取首页统计数据
     */
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        Map<String, Object> data = new HashMap<>();
        boolean isAdmin = SecurityUtils.isAdmin() || SecurityUtils.hasRole("admin");
        boolean isDoctor = SecurityUtils.hasRole("doctor");
        Long userId = SecurityUtils.getUserId();

        // 1. 患者总数 & 医生总数
        SysUser userQuery = new SysUser();
        List<SysUser> allUsers = userService.selectUserList(userQuery);
        
        long patientCount = 0;
        long doctorCount = allUsers.stream().filter(u -> u.isAdmin() || u.getRoles().stream().anyMatch(r -> "doctor".equals(r.getRoleKey()))).count();

        List<Long> myPatientIds = null;

        if (isAdmin) {
            // 管理员：看所有患者
            patientCount = allUsers.stream().filter(u -> u.getRoles().stream().anyMatch(r -> "patient".equals(r.getRoleKey()))).count();
        } else if (isDoctor) {
            // 医生：看我的患者
            SysUser docQuery = new SysUser();
            docQuery.getParams().put("doctorId", userId);
            List<SysUser> myPatients = doctorPatientService.selectPatientListByDoctorId(docQuery);
            patientCount = myPatients.size();
            myPatientIds = myPatients.stream().map(SysUser::getUserId).collect(Collectors.toList());
        } else {
            // 患者：看自己
            patientCount = 1;
        }

        List<Long> finalMyPatientIds = myPatientIds;

        // 2. 健康预警 (未读/总数) & 最新预警列表
        HealthWarning warningQuery = new HealthWarning();
        if (!isAdmin) {
            // Updated: Doctors now receive their own warning copies (Double Send), 
            // so they just query by their own userId like patients.
            warningQuery.setUserId(userId);
        }
        List<HealthWarning> warnings = warningService.selectHealthWarningList(warningQuery);
        
        long warningCount = warnings.size();
        List<HealthWarning> recentWarnings = warnings.stream()
            .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
            .limit(5)
            .collect(Collectors.toList());

        // 3. 今日随访
        FollowupRecord recordQuery = new FollowupRecord();
        // 简单处理：查询所有记录，然后内存过滤今日 (优化：应在SQL处理)
        List<FollowupRecord> records = recordService.selectFollowupRecordList(recordQuery);
        String today = DateUtils.getDate();
        
        long todayFollowups = records.stream().filter(r -> {
            boolean isToday = DateUtils.parseDateToStr("yyyy-MM-dd", r.getRecordDate()).equals(today);
            boolean scopeMatch = true;
            if (isDoctor && finalMyPatientIds != null) {
                scopeMatch = finalMyPatientIds.contains(r.getPatientId());
            } else if (!isAdmin && !isDoctor) {
                scopeMatch = r.getPatientId().equals(userId);
            }
            return isToday && scopeMatch;
        }).count();

        // 4. 图表数据 - 健康状态分布 (基于预警等级)
        Map<String, Long> pieData = warnings.stream()
            .collect(Collectors.groupingBy(HealthWarning::getWarningLevel, Collectors.counting()));
        
        // 5. 图表数据 - 近7日患者增长趋势 (真实数据)
        List<String> trendDates = new java.util.ArrayList<>();
        List<Long> trendData = new java.util.ArrayList<>();
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -6); // Start from 6 days ago

        for (int i = 0; i < 7; i++) {
            String dateStr = DateUtils.parseDateToStr("yyyy-MM-dd", cal.getTime());
            trendDates.add(dateStr);
            
            long count = allUsers.stream().filter(u -> {
                boolean isPatient = u.getRoles().stream().anyMatch(r -> "patient".equals(r.getRoleKey()));
                boolean matchDate = DateUtils.parseDateToStr("yyyy-MM-dd", u.getCreateTime()).equals(dateStr);
                return isPatient && matchDate;
            }).count();
            
            trendData.add(count);
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }
        
        data.put("patientCount", patientCount);
        data.put("doctorCount", doctorCount);
        data.put("warningCount", warningCount);
        data.put("todayFollowups", todayFollowups);
        data.put("recentWarnings", recentWarnings);
        data.put("pieData", pieData);
        data.put("trendDates", trendDates);
        data.put("trendData", trendData);

        return success(data);
    }
}
