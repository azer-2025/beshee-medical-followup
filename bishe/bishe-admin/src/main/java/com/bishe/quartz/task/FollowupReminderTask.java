package com.bishe.quartz.task;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import com.bishe.common.utils.DateUtils;
import com.bishe.common.utils.StringUtils;
import com.bishe.system.domain.FollowupPlan;
import com.bishe.system.domain.FollowupRecord;
import com.bishe.system.domain.SysNotice;
import com.bishe.system.service.IFollowupPlanService;
import com.bishe.system.service.IFollowupRecordService;
import com.bishe.system.service.ISysNoticeService;
import com.bishe.web.service.followup.FollowupPlanScheduleService;

@Component("followupReminderTask")
public class FollowupReminderTask
{
    private static final Logger log = LoggerFactory.getLogger(FollowupReminderTask.class);

    private static final int MAX_MEDICATION_REMINDERS = 3;

    private static final int MEDICATION_REMIND_INTERVAL_MINUTES = 120;

    private final IFollowupPlanService followupPlanService;
    private final IFollowupRecordService followupRecordService;
    private final ISysNoticeService noticeService;
    private final ObjectProvider<FollowupPlanScheduleService> followupPlanScheduleServiceProvider;

    public FollowupReminderTask(IFollowupPlanService followupPlanService, IFollowupRecordService followupRecordService,
            ISysNoticeService noticeService, ObjectProvider<FollowupPlanScheduleService> followupPlanScheduleServiceProvider)
    {
        this.followupPlanService = followupPlanService;
        this.followupRecordService = followupRecordService;
        this.noticeService = noticeService;
        this.followupPlanScheduleServiceProvider = followupPlanScheduleServiceProvider;
    }

    public void dispatchPlan(Long planId)
    {
        FollowupPlan plan = followupPlanService.selectFollowupPlanById(planId);
        if (plan == null || !"0".equals(plan.getStatus()) || StringUtils.isBlank(plan.getCronExpression()))
        {
            return;
        }

        LocalDate today = LocalDate.now();
        if (plan.getStartDate() != null && today.isBefore(toLocalDate(plan.getStartDate())))
        {
            return;
        }

        if (plan.getEndDate() != null && today.isAfter(toLocalDate(plan.getEndDate())))
        {
            plan.setStatus("1");
            followupPlanService.updateFollowupPlan(plan);
            FollowupPlanScheduleService scheduleService = followupPlanScheduleServiceProvider.getIfAvailable();
            if (scheduleService != null)
            {
                scheduleService.syncPlanSchedule(plan, "system");
            }
            return;
        }

        Date recordDate = DateUtils.toDate(today);
        FollowupRecord existingRecord = followupRecordService.selectFollowupRecordByPlanAndDate(planId, recordDate);
        if (existingRecord != null)
        {
            return;
        }

        Date now = DateUtils.getNowDate();
        FollowupRecord record = new FollowupRecord();
        record.setPlanId(plan.getPlanId());
        record.setPatientId(plan.getPatientId());
        record.setDoctorId(plan.getDoctorId());
        record.setRecordDate(recordDate);
        record.setStatus("0");
        record.setDoctorAdvice(buildDispatchContent(plan));
        if (isMedicationPlan(plan))
        {
            record.setReminderCount(1);
            record.setLastReminderTime(now);
        }
        followupRecordService.insertFollowupRecord(record);
        createDispatchNotice(plan, recordDate, isMedicationPlan(plan) ? 1 : 0);
        log.info("Auto dispatched follow-up reminder, planId={}, patientId={}", plan.getPlanId(), plan.getPatientId());
    }

    public void remindOverdueMedicationRecords()
    {
        List<FollowupRecord> overdueRecords = followupRecordService.selectOverdueMedicationReminderRecords(
                MAX_MEDICATION_REMINDERS, MEDICATION_REMIND_INTERVAL_MINUTES);
        if (overdueRecords == null || overdueRecords.isEmpty())
        {
            return;
        }

        Date now = DateUtils.getNowDate();
        for (FollowupRecord record : overdueRecords)
        {
            FollowupPlan plan = followupPlanService.selectFollowupPlanById(record.getPlanId());
            if (plan == null || !isMedicationPlan(plan))
            {
                continue;
            }

            int nextReminderCount = (record.getReminderCount() == null ? 0 : record.getReminderCount()) + 1;
            followupRecordService.updateMedicationReminderState(record.getRecordId(), nextReminderCount, now);
            createMedicationOverdueNotice(plan, record, nextReminderCount);
            log.info("Medication reminder resent, recordId={}, planId={}, reminderCount={}",
                    record.getRecordId(), record.getPlanId(), nextReminderCount);
        }
    }

    private boolean isMedicationPlan(FollowupPlan plan)
    {
        return plan != null && "medication".equals(plan.getModuleType());
    }

    private String buildDispatchContent(FollowupPlan plan)
    {
        StringBuilder builder = new StringBuilder("系统已自动派发");
        builder.append(resolveModuleName(plan.getModuleType())).append("任务");
        if (StringUtils.isNotBlank(plan.getReminderDescription()))
        {
            builder.append("：").append(plan.getReminderDescription());
        }
        else if (StringUtils.isNotBlank(plan.getPlanContent()))
        {
            builder.append("：").append(plan.getPlanContent());
        }
        if (StringUtils.isNotBlank(plan.getNotifyChannel()))
        {
            builder.append("（通知渠道：").append(resolveChannelName(plan.getNotifyChannel())).append("）");
        }
        if (isMedicationPlan(plan))
        {
            builder.append("。请患者按时反馈服药结果，逾期未反馈将自动追加提醒。");
        }
        return builder.toString();
    }

    private void createDispatchNotice(FollowupPlan plan, Date recordDate, int reminderRound)
    {
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(buildNoticeTitle(plan, reminderRound));
        notice.setNoticeType("1");
        notice.setStatus("0");
        notice.setCreateBy("system");
        notice.setRemark(buildNoticeRemark(plan, reminderRound));
        notice.setNoticeContent(buildDispatchNoticeContent(plan, recordDate, reminderRound));
        noticeService.insertNotice(notice);
    }

    private void createMedicationOverdueNotice(FollowupPlan plan, FollowupRecord record, int reminderRound)
    {
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(buildNoticeTitle(plan, reminderRound));
        notice.setNoticeType("1");
        notice.setStatus("0");
        notice.setCreateBy("system");
        notice.setRemark(buildNoticeRemark(plan, reminderRound));
        notice.setNoticeContent(buildMedicationOverdueNoticeContent(plan, record, reminderRound));
        noticeService.insertNotice(notice);
    }

    private String buildNoticeTitle(FollowupPlan plan, int reminderRound)
    {
        StringBuilder title = new StringBuilder("随访提醒-");
        title.append(resolveModuleName(plan.getModuleType()));
        if (StringUtils.isNotBlank(plan.getPatientName()))
        {
            title.append("-").append(plan.getPatientName());
        }
        if (reminderRound > 1)
        {
            title.append("-第").append(reminderRound).append("次提醒");
        }
        String titleText = title.toString();
        return titleText.length() > 50 ? titleText.substring(0, 50) : titleText;
    }

    private String buildDispatchNoticeContent(FollowupPlan plan, Date recordDate, int reminderRound)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("计划标题：").append(StringUtils.defaultIfBlank(plan.getPlanTitle(), "未命名计划"));
        builder.append("<br/>提醒模块：").append(resolveModuleName(plan.getModuleType()));
        builder.append("<br/>目标患者：").append(StringUtils.defaultIfBlank(plan.getPatientName(), "-"));
        builder.append("<br/>责任医护：").append(StringUtils.defaultIfBlank(plan.getDoctorName(), "-"));
        builder.append("<br/>执行日期：").append(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, recordDate));
        builder.append("<br/>通知渠道：").append(resolveChannelName(plan.getNotifyChannel()));
        if (StringUtils.isNotBlank(plan.getReminderDescription()))
        {
            builder.append("<br/>提醒说明：").append(plan.getReminderDescription());
        }
        if (isMedicationPlan(plan))
        {
            builder.append("<br/>用药反馈：请患者收到提醒后反馈已服药、延后服药或未服药。");
            if (reminderRound > 0)
            {
                builder.append("<br/>提醒轮次：首次派发提醒");
            }
        }
        appendSmsSimulation(builder, plan.getNotifyChannel());
        return builder.toString();
    }

    private String buildMedicationOverdueNoticeContent(FollowupPlan plan, FollowupRecord record, int reminderRound)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("计划标题：").append(StringUtils.defaultIfBlank(plan.getPlanTitle(), "未命名计划"));
        builder.append("<br/>提醒模块：").append(resolveModuleName(plan.getModuleType()));
        builder.append("<br/>目标患者：").append(StringUtils.defaultIfBlank(record.getPatientName(), StringUtils.defaultIfBlank(plan.getPatientName(), "-")));
        builder.append("<br/>责任医护：").append(StringUtils.defaultIfBlank(record.getDoctorName(), StringUtils.defaultIfBlank(plan.getDoctorName(), "-")));
        builder.append("<br/>随访日期：").append(record.getRecordDate() == null ? "-" : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, record.getRecordDate()));
        builder.append("<br/>逾期提醒：患者尚未反馈服药结果，系统已发出第").append(reminderRound).append("次提醒。");
        builder.append("<br/>反馈要求：患者需返回已服药、延后服药或未服药结果。");
        appendSmsSimulation(builder, plan.getNotifyChannel());
        return builder.toString();
    }

    private String buildNoticeRemark(FollowupPlan plan, int reminderRound)
    {
        return "followup-notice|planId:" + plan.getPlanId()
                + "|doctorId:" + plan.getDoctorId()
                + "|patientId:" + plan.getPatientId()
                + "|round:" + reminderRound;
    }

    private void appendSmsSimulation(StringBuilder builder, String notifyChannel)
    {
        if (containsSmsSimulation(notifyChannel))
        {
            builder.append("<br/>短信状态：仅模拟记录，不调用真实短信网关。");
        }
    }

    private String resolveModuleName(String moduleType)
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
        return "随访提醒";
    }

    private String resolveChannelName(String notifyChannel)
    {
        if ("websocket".equals(notifyChannel))
        {
            return "站内通知";
        }
        if ("sms".equals(notifyChannel))
        {
            return "短信模拟";
        }
        if ("all".equals(notifyChannel))
        {
            return "站内通知 + 短信模拟";
        }
        return "未设置";
    }

    private boolean containsSmsSimulation(String notifyChannel)
    {
        return "sms".equals(notifyChannel) || "all".equals(notifyChannel);
    }

    private LocalDate toLocalDate(Date date)
    {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
