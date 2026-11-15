package com.bishe.system.service.impl;

import java.util.List;
import com.bishe.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bishe.system.mapper.FollowupRecordMapper;
import com.bishe.system.domain.FollowupRecord;
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

    /**
     * 查询随访记录
     * 
     * @param recordId 随访记录主键
     * @return 随访记录
     */
    @Override
    public FollowupRecord selectFollowupRecordById(Long recordId)
    {
        return followupRecordMapper.selectFollowupRecordById(recordId);
    }

    /**
     * 查询随访记录列表
     * 
     * @param followupRecord 随访记录
     * @return 随访记录
     */
    @Override
    public List<FollowupRecord> selectFollowupRecordList(FollowupRecord followupRecord)
    {
        return followupRecordMapper.selectFollowupRecordList(followupRecord);
    }

    /**
     * 新增随访记录
     * 
     * @param followupRecord 随访记录
     * @return 结果
     */
    @Override
    public int insertFollowupRecord(FollowupRecord followupRecord)
    {
        followupRecord.setCreateTime(DateUtils.getNowDate());
        return followupRecordMapper.insertFollowupRecord(followupRecord);
    }

    /**
     * 修改随访记录
     * 
     * @param followupRecord 随访记录
     * @return 结果
     */
    @Override
    public int updateFollowupRecord(FollowupRecord followupRecord)
    {
        followupRecord.setUpdateTime(DateUtils.getNowDate());
        return followupRecordMapper.updateFollowupRecord(followupRecord);
    }

    /**
     * 批量删除随访记录
     * 
     * @param recordIds 需要删除的随访记录主键
     * @return 结果
     */
    @Override
    public int deleteFollowupRecordByIds(Long[] recordIds)
    {
        return followupRecordMapper.deleteFollowupRecordByIds(recordIds);
    }

    /**
     * 删除随访记录信息
     * 
     * @param recordId 随访记录主键
     * @return 结果
     */
    @Override
    public int deleteFollowupRecordById(Long recordId)
    {
        return followupRecordMapper.deleteFollowupRecordById(recordId);
    }
}
