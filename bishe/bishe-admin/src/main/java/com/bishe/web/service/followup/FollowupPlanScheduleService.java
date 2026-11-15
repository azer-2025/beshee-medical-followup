package com.bishe.web.service.followup;

import java.util.List;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bishe.common.constant.ScheduleConstants;
import com.bishe.common.exception.ServiceException;
import com.bishe.common.exception.job.TaskException;
import com.bishe.common.utils.StringUtils;
import com.bishe.quartz.domain.SysJob;
import com.bishe.quartz.service.ISysJobService;
import com.bishe.system.domain.FollowupPlan;
import com.bishe.system.service.IFollowupPlanService;

@Service
public class FollowupPlanScheduleService
{
    private static final Logger log = LoggerFactory.getLogger(FollowupPlanScheduleService.class);

    private static final String JOB_GROUP = "FOLLOWUP_PLAN";

    private final IFollowupPlanService followupPlanService;
    private final ISysJobService sysJobService;

    public FollowupPlanScheduleService(IFollowupPlanService followupPlanService, ISysJobService sysJobService)
    {
        this.followupPlanService = followupPlanService;
        this.sysJobService = sysJobService;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertFollowupPlan(FollowupPlan followupPlan, String operator)
    {
        int rows = followupPlanService.insertFollowupPlan(followupPlan);
        if (rows > 0)
        {
            syncPlanSchedule(followupPlan, operator);
        }
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateFollowupPlan(FollowupPlan followupPlan, String operator)
    {
        int rows = followupPlanService.updateFollowupPlan(followupPlan);
        if (rows > 0)
        {
            syncPlanSchedule(followupPlan, operator);
        }
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteFollowupPlanByIds(Long[] planIds)
    {
        for (Long planId : planIds)
        {
            deletePlanSchedule(planId);
        }
        return followupPlanService.deleteFollowupPlanByIds(planIds);
    }

    public void syncAllPlansOnStartup()
    {
        FollowupPlan query = new FollowupPlan();
        List<FollowupPlan> plans = followupPlanService.selectFollowupPlanList(query);
        for (FollowupPlan plan : plans)
        {
            try
            {
                syncPlanSchedule(plan, "system");
            }
            catch (Exception ex)
            {
                log.warn("同步随访计划调度失败, planId={}", plan.getPlanId(), ex);
            }
        }
    }

    public void syncPlanSchedule(FollowupPlan plan, String operator)
    {
        try
        {
            upsertScheduleJob(plan, operator);
        }
        catch (SchedulerException | TaskException ex)
        {
            throw new ServiceException("同步随访调度任务失败: " + ex.getMessage());
        }
    }

    public void deletePlanSchedule(Long planId)
    {
        try
        {
            SysJob existingJob = findJobByPlanId(planId);
            if (existingJob != null)
            {
                sysJobService.deleteJob(existingJob);
            }
        }
        catch (SchedulerException ex)
        {
            throw new ServiceException("删除随访调度任务失败: " + ex.getMessage());
        }
    }

    private void upsertScheduleJob(FollowupPlan plan, String operator) throws SchedulerException, TaskException
    {
        if (plan == null || plan.getPlanId() == null)
        {
            return;
        }

        SysJob existingJob = findJobByPlanId(plan.getPlanId());
        if (StringUtils.isBlank(plan.getCronExpression()))
        {
            if (existingJob != null)
            {
                sysJobService.deleteJob(existingJob);
            }
            return;
        }

        if (!sysJobService.checkCronExpressionIsValid(plan.getCronExpression()))
        {
            throw new ServiceException("Cron表达式不正确，无法创建提醒任务");
        }

        SysJob job = buildJob(plan, operator, existingJob);
        if (existingJob == null)
        {
            boolean shouldResume = ScheduleConstants.Status.NORMAL.getValue().equals(job.getStatus());
            sysJobService.insertJob(job);
            if (shouldResume)
            {
                job.setStatus(ScheduleConstants.Status.NORMAL.getValue());
                sysJobService.changeStatus(job);
            }
            return;
        }

        sysJobService.updateJob(job);
    }

    private SysJob buildJob(FollowupPlan plan, String operator, SysJob existingJob)
    {
        SysJob job = new SysJob();
        if (existingJob != null)
        {
            job.setJobId(existingJob.getJobId());
        }
        job.setJobName(buildJobName(plan));
        job.setJobGroup(JOB_GROUP);
        job.setInvokeTarget(buildInvokeTarget(plan.getPlanId()));
        job.setCronExpression(plan.getCronExpression());
        job.setMisfirePolicy(ScheduleConstants.MISFIRE_DO_NOTHING);
        job.setConcurrent("1");
        job.setStatus(isPlanActive(plan) ? ScheduleConstants.Status.NORMAL.getValue() : ScheduleConstants.Status.PAUSE.getValue());
        job.setRemark(buildRemark(plan));
        if (existingJob == null)
        {
            job.setCreateBy(operator);
        }
        else
        {
            job.setUpdateBy(operator);
        }
        return job;
    }

    private SysJob findJobByPlanId(Long planId)
    {
        SysJob query = new SysJob();
        query.setJobGroup(JOB_GROUP);
        List<SysJob> jobs = sysJobService.selectJobList(query);
        String invokeTarget = buildInvokeTarget(planId);
        for (SysJob job : jobs)
        {
            if (invokeTarget.equals(job.getInvokeTarget()))
            {
                return job;
            }
        }
        return null;
    }

    private boolean isPlanActive(FollowupPlan plan)
    {
        return "0".equals(plan.getStatus());
    }

    private String buildJobName(FollowupPlan plan)
    {
        String title = StringUtils.isNotBlank(plan.getPlanTitle()) ? plan.getPlanTitle() : "随访提醒计划" + plan.getPlanId();
        String jobName = "随访提醒-" + title;
        return jobName.length() > 64 ? jobName.substring(0, 64) : jobName;
    }

    private String buildInvokeTarget(Long planId)
    {
        return "followupReminderTask.dispatchPlan(" + planId + "L)";
    }

    private String buildRemark(FollowupPlan plan)
    {
        StringBuilder builder = new StringBuilder("随访提醒自动派发");
        if (StringUtils.isNotBlank(plan.getPlanTitle()))
        {
            builder.append(" - ").append(plan.getPlanTitle());
        }
        if (StringUtils.isNotBlank(plan.getModuleType()))
        {
            builder.append(" / 模块: ").append(formatModuleType(plan.getModuleType()));
        }
        if (StringUtils.isNotBlank(plan.getTargetGroup()))
        {
            builder.append(" / 分组: ").append(plan.getTargetGroup());
        }
        return builder.toString();
    }

    private String formatModuleType(String moduleType)
    {
        if ("medication".equals(moduleType))
        {
            return "用药提醒";
        }
        if ("measurement".equals(moduleType))
        {
            return "指标测量";
        }
        if ("rehab".equals(moduleType))
        {
            return "康复训练";
        }
        if ("revisit".equals(moduleType))
        {
            return "复诊通知";
        }
        return moduleType;
    }
}
