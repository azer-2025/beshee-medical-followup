package com.bishe.system.service;

import java.util.Date;
import java.util.List;
import com.bishe.system.domain.FollowupRecord;

/**
 * 随访记录Service接口
 * 
 * @author bishe
 * @date 2025-05-22
 */
public interface IFollowupRecordService 
{
    /**
     * 查询随访记录
     * 
     * @param recordId 随访记录主键
     * @return 随访记录
     */
    public FollowupRecord selectFollowupRecordById(Long recordId);

    /**
     * 查询随访记录列表
     * 
     * @param followupRecord 随访记录
     * @return 随访记录集合
     */
    public List<FollowupRecord> selectFollowupRecordList(FollowupRecord followupRecord);

    /**
     * 按计划与随访日期查询记录
     *
     * @param planId 计划ID
     * @param recordDate 随访日期
     * @return 随访记录
     */
    public FollowupRecord selectFollowupRecordByPlanAndDate(Long planId, Date recordDate);

    /**
     * 查询逾期未反馈的用药提醒记录
     *
     * @param maxReminderCount 最大提醒次数
     * @param remindIntervalMinutes 提醒间隔（分钟）
     * @return 随访记录集合
     */
    public List<FollowupRecord> selectOverdueMedicationReminderRecords(int maxReminderCount, int remindIntervalMinutes);

    /**
     * 新增随访记录
     * 
     * @param followupRecord 随访记录
     * @return 结果
     */
    public int insertFollowupRecord(FollowupRecord followupRecord);

    /**
     * 修改随访记录
     * 
     * @param followupRecord 随访记录
     * @return 结果
     */
    public int updateFollowupRecord(FollowupRecord followupRecord);

    /**
     * 更新用药提醒状态
     *
     * @param recordId 记录ID
     * @param reminderCount 已提醒次数
     * @param lastReminderTime 最近提醒时间
     * @return 结果
     */
    public int updateMedicationReminderState(Long recordId, Integer reminderCount, Date lastReminderTime);

    /**
     * 批量删除随访记录
     * 
     * @param recordIds 需要删除的随访记录主键集合
     * @return 结果
     */
    public int deleteFollowupRecordByIds(Long[] recordIds);

    /**
     * 删除随访记录信息
     * 
     * @param recordId 随访记录主键
     * @return 结果
     */
    public int deleteFollowupRecordById(Long recordId);
}
