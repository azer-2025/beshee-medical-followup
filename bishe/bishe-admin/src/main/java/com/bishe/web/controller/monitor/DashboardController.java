package com.bishe.web.controller.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bishe.common.core.controller.BaseController;
import com.bishe.common.core.domain.AjaxResult;
import com.bishe.common.core.domain.entity.SysRole;
import com.bishe.common.core.domain.entity.SysUser;
import com.bishe.common.utils.DateUtils;
import com.bishe.common.utils.SecurityUtils;
import com.bishe.system.domain.FollowupPlan;
import com.bishe.system.domain.FollowupRecord;
import com.bishe.system.domain.HealthArchive;
import com.bishe.system.domain.HealthWarning;
import com.bishe.system.service.IDoctorPatientService;
import com.bishe.system.service.IFollowupPlanService;
import com.bishe.system.service.IFollowupRecordService;
import com.bishe.system.service.IHealthArchiveService;
import com.bishe.system.service.IHealthWarningService;
import com.bishe.system.service.ISysUserService;

/**
 * Dashboard statistics.
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
    private IFollowupPlanService planService;

    @Autowired
    private IHealthArchiveService archiveService;

    @Autowired
    private IDoctorPatientService doctorPatientService;

    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        Map<String, Object> data = new HashMap<>();
        boolean isAdmin = SecurityUtils.isAdmin() || SecurityUtils.hasRole("admin");
        boolean isDoctor = SecurityUtils.hasRole("doctor");
        Long userId = SecurityUtils.getUserId();

        SysUser userQuery = new SysUser();
        List<SysUser> allUsers = userService.selectUserList(userQuery);

        long patientCount = 0L;
        long doctorCount = allUsers.stream().filter(user -> hasRoleKey(user, "doctor") || user.isAdmin()).count();
        List<Long> myPatientIds = null;

        if (isAdmin)
        {
            patientCount = allUsers.stream().filter(user -> hasRoleKey(user, "patient")).count();
        }
        else if (isDoctor)
        {
            SysUser doctorQuery = new SysUser();
            doctorQuery.getParams().put("doctorId", userId);
            List<SysUser> myPatients = doctorPatientService.selectPatientListByDoctorId(doctorQuery);
            patientCount = myPatients.size();
            myPatientIds = myPatients.stream().map(SysUser::getUserId).filter(Objects::nonNull).collect(Collectors.toList());
        }
        else
        {
            patientCount = 1L;
        }

        List<Long> scopedPatientIds = myPatientIds;

        HealthWarning warningQuery = new HealthWarning();
        if (!isAdmin)
        {
            warningQuery.setUserId(userId);
        }
        List<HealthWarning> warnings = warningService.selectHealthWarningList(warningQuery);
        long warningCount = warnings.size();
        List<HealthWarning> recentWarnings = warnings.stream()
            .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
            .limit(5)
            .collect(Collectors.toList());

        FollowupRecord recordQuery = new FollowupRecord();
        List<FollowupRecord> records = recordService.selectFollowupRecordList(recordQuery);
        String today = DateUtils.getDate();
        long todayFollowups = records.stream().filter(record -> {
            if (record.getRecordDate() == null)
            {
                return false;
            }
            boolean isToday = today.equals(DateUtils.parseDateToStr("yyyy-MM-dd", record.getRecordDate()));
            return isToday && inScope(record.getPatientId(), isAdmin, isDoctor, userId, scopedPatientIds);
        }).count();

        Map<String, Long> pieData = warnings.stream()
            .collect(Collectors.groupingBy(warning -> defaultLabel(warning.getWarningLevel(), "1"), Collectors.counting()));

        List<String> trendDates = new ArrayList<>();
        List<Long> trendData = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        for (int i = 0; i < 7; i++)
        {
            String dateStr = DateUtils.parseDateToStr("yyyy-MM-dd", calendar.getTime());
            trendDates.add(dateStr);
            long count = allUsers.stream().filter(user -> {
                if (!hasRoleKey(user, "patient") || user.getCreateTime() == null)
                {
                    return false;
                }
                return dateStr.equals(DateUtils.parseDateToStr("yyyy-MM-dd", user.getCreateTime()));
            }).count();
            trendData.add(count);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        FollowupPlan planQuery = new FollowupPlan();
        HealthArchive archiveQuery = new HealthArchive();
        if (!isAdmin)
        {
            if (isDoctor)
            {
                planQuery.setDoctorId(userId);
                archiveQuery.getParams().put("doctorId", userId);
            }
            else
            {
                planQuery.setPatientId(userId);
                archiveQuery.setUserId(userId);
            }
        }

        List<FollowupPlan> plans = planService.selectFollowupPlanList(planQuery);
        List<HealthArchive> archives = archiveService.selectHealthArchiveList(archiveQuery);

        Map<String, Long> diseaseData = archives.stream()
            .collect(Collectors.groupingBy(archive -> defaultLabel(archive.getDiseaseType(), "未分组"), Collectors.counting()));
        Map<String, Long> riskData = archives.stream()
            .collect(Collectors.groupingBy(archive -> defaultLabel(archive.getRiskLevel(), "未评估"), Collectors.counting()));
        Map<String, Long> ageStageData = archives.stream()
            .collect(Collectors.groupingBy(archive -> defaultLabel(archive.getAgeStage(), "未分层"), Collectors.counting()));
        Map<String, Long> funnelData = plans.stream()
            .collect(Collectors.groupingBy(plan -> defaultPlanStatusName(plan.getStatus()), Collectors.counting()));

        List<String> diseaseNames = new ArrayList<>(diseaseData.keySet());
        List<String> riskLevels = new ArrayList<>(riskData.keySet());

        Map<String, Map<String, Long>> matrix = archives.stream().collect(Collectors.groupingBy(
            archive -> defaultLabel(archive.getDiseaseType(), "未分组"),
            Collectors.groupingBy(archive -> defaultLabel(archive.getRiskLevel(), "未评估"), Collectors.counting())));

        List<List<Object>> diseaseRiskHeatmap = new ArrayList<>();
        for (int diseaseIndex = 0; diseaseIndex < diseaseNames.size(); diseaseIndex++)
        {
            String diseaseName = diseaseNames.get(diseaseIndex);
            Map<String, Long> riskMap = matrix.getOrDefault(diseaseName, new HashMap<>());
            for (int riskIndex = 0; riskIndex < riskLevels.size(); riskIndex++)
            {
                String riskLevel = riskLevels.get(riskIndex);
                diseaseRiskHeatmap.add(Arrays.asList(diseaseIndex, riskIndex, riskMap.getOrDefault(riskLevel, 0L)));
            }
        }

        data.put("patientCount", patientCount);
        data.put("doctorCount", doctorCount);
        data.put("warningCount", warningCount);
        data.put("todayFollowups", todayFollowups);
        data.put("recentWarnings", recentWarnings);
        data.put("pieData", pieData);
        data.put("trendDates", trendDates);
        data.put("trendData", trendData);
        data.put("diseaseData", diseaseData);
        data.put("riskData", riskData);
        data.put("ageStageData", ageStageData);
        data.put("funnelData", funnelData);
        data.put("diseaseNames", diseaseNames);
        data.put("riskLevels", riskLevels);
        data.put("diseaseRiskHeatmap", diseaseRiskHeatmap);
        return success(data);
    }

    private boolean hasRoleKey(SysUser user, String roleKey)
    {
        if (user == null || user.getRoles() == null)
        {
            return false;
        }
        return user.getRoles().stream().map(SysRole::getRoleKey).anyMatch(roleKey::equals);
    }

    private boolean inScope(Long patientId, boolean isAdmin, boolean isDoctor, Long userId, List<Long> myPatientIds)
    {
        if (patientId == null)
        {
            return false;
        }
        if (isAdmin)
        {
            return true;
        }
        if (isDoctor)
        {
            return myPatientIds != null && myPatientIds.contains(patientId);
        }
        return patientId.equals(userId);
    }

    private String defaultLabel(String value, String fallback)
    {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String defaultPlanStatusName(String status)
    {
        if ("1".equals(status))
        {
            return "已完成";
        }
        if ("2".equals(status))
        {
            return "已取消";
        }
        return "进行中";
    }
}
