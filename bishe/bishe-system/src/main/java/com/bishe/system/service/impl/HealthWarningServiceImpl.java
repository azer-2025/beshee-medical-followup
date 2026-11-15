package com.bishe.system.service.impl;

import java.util.List;
import com.bishe.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bishe.system.mapper.HealthWarningMapper;
import com.bishe.system.domain.HealthWarning;
import com.bishe.system.service.IHealthWarningService;

/**
 * 健康预警Service业务层处理
 * 
 * @author bishe
 * @date 2025-05-22
 */
@Service
public class HealthWarningServiceImpl implements IHealthWarningService 
{
    @Autowired
    private HealthWarningMapper healthWarningMapper;

    /**
     * 查询健康预警
     * 
     * @param warningId 健康预警主键
     * @return 健康预警
     */
    @Override
    public HealthWarning selectHealthWarningById(Long warningId)
    {
        return healthWarningMapper.selectHealthWarningById(warningId);
    }

    /**
     * 查询健康预警列表
     * 
     * @param healthWarning 健康预警
     * @return 健康预警
     */
    @Override
    public List<HealthWarning> selectHealthWarningList(HealthWarning healthWarning)
    {
        return healthWarningMapper.selectHealthWarningList(healthWarning);
    }

    /**
     * 新增健康预警
     * 
     * @param healthWarning 健康预警
     * @return 结果
     */
    @Override
    public int insertHealthWarning(HealthWarning healthWarning)
    {
        healthWarning.setCreateTime(DateUtils.getNowDate());
        return healthWarningMapper.insertHealthWarning(healthWarning);
    }

    /**
     * 修改健康预警
     * 
     * @param healthWarning 健康预警
     * @return 结果
     */
    @Override
    public int updateHealthWarning(HealthWarning healthWarning)
    {
        return healthWarningMapper.updateHealthWarning(healthWarning);
    }

    /**
     * 批量删除健康预警
     * 
     * @param warningIds 需要删除的健康预警主键
     * @return 结果
     */
    @Override
    public int deleteHealthWarningByIds(Long[] warningIds)
    {
        return healthWarningMapper.deleteHealthWarningByIds(warningIds);
    }

    /**
     * 删除健康预警信息
     * 
     * @param warningId 健康预警主键
     * @return 结果
     */
    @Override
    public int deleteHealthWarningById(Long warningId)
    {
        return healthWarningMapper.deleteHealthWarningById(warningId);
    }
}
