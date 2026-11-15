package com.bishe.system.service;

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
