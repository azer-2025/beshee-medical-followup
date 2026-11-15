package com.bishe.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bishe.common.utils.DateUtils;
import com.bishe.system.domain.FollowupRecord;
import com.bishe.system.mapper.FollowupRecordMapper;
import com.bishe.system.service.IFollowupRecordService;

/**
 * 随访记录Service业务层处理
 *
 * @author bishe
 * @date 2025-05-22
 */
@Service
public class FollowupRecordServiceImpl implements IFollowupRecordService
{
    @Autowired
    private FollowupRecordMapper followupRecordMapper;

    @Override
    public FollowupRecord selectFollowupRecordById(Long recordId)
    {
        return followupRecordMapper.selectFollowupRecordById(recordId);
    }

    @Override
    public List<FollowupRecord> selectFollowupRecordList(FollowupRecord followupRecord)
    {
        return followupRecordMapper.selectFollowupRecordList(followupRecord);
    }

    @Override
    public FollowupRecord selectFollowupRecordByPlanAndDate(Long planId, Date recordDate)
    {
        return followupRecordMapper.selectFollowupRecordByPlanAndDate(planId, recordDate);
    }

    @Override
    public List<FollowupRecord> selectOverdueMedicationReminderRecords(int maxReminderCount, int remindIntervalMinutes)
    {
        return followupRecordMapper.selectOverdueMedicationReminderRecords(maxReminderCount, remindIntervalMinutes);
    }

    @Override
    public int insertFollowupRecord(FollowupRecord followupRecord)
    {
        followupRecord.setCreateTime(DateUtils.getNowDate());
        return followupRecordMapper.insertFollowupRecord(followupRecord);
    }

    @Override
    public int updateFollowupRecord(FollowupRecord followupRecord)
    {
        followupRecord.setUpdateTime(DateUtils.getNowDate());
        return followupRecordMapper.updateFollowupRecord(followupRecord);
    }

    @Override
    public int updateMedicationReminderState(Long recordId, Integer reminderCount, Date lastReminderTime)
    {
        return followupRecordMapper.updateMedicationReminderState(recordId, reminderCount, lastReminderTime);
    }

    @Override
    public int deleteFollowupRecordByIds(Long[] recordIds)
    {
        return followupRecordMapper.deleteFollowupRecordByIds(recordIds);
    }

    @Override
    public int deleteFollowupRecordById(Long recordId)
    {
        return followupRecordMapper.deleteFollowupRecordById(recordId);
    }
}
