package com.bishe.system.mapper;

import java.util.List;
import com.bishe.system.domain.HealthWarning;

/**
 * 健康预警Mapper接口
 * 
 * @author bishe
 * @date 2025-05-22
 */
public interface HealthWarningMapper 
{
    /**
     * 查询健康预警
     * 
     * @param warningId 健康预警主键
     * @return 健康预警
     */
    public HealthWarning selectHealthWarningById(Long warningId);

    /**
     * 查询健康预警列表
     * 
     * @param healthWarning 健康预警
     * @return 健康预警集合
     */
    public List<HealthWarning> selectHealthWarningList(HealthWarning healthWarning);

    /**
     * 新增健康预警
     * 
     * @param healthWarning 健康预警
     * @return 结果
     */
    public int insertHealthWarning(HealthWarning healthWarning);

    /**
     * 修改健康预警
     * 
     * @param healthWarning 健康预警
     * @return 结果
     */
    public int updateHealthWarning(HealthWarning healthWarning);

    /**
     * 删除健康预警
     * 
     * @param warningId 健康预警主键
     * @return 结果
     */
    public int deleteHealthWarningById(Long warningId);

    /**
     * 批量删除健康预警
     * 
     * @param warningIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHealthWarningByIds(Long[] warningIds);
}
