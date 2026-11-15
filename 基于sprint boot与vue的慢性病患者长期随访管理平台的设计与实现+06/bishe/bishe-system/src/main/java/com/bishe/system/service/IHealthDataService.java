package com.bishe.system.service;

import java.util.List;
import com.bishe.system.domain.HealthData;

/**
 * 健康数据采集Service接口
 * 
 * @author bishe
 * @date 2025-05-22
 */
public interface IHealthDataService 
{
    /**
     * 查询健康数据采集
     * 
     * @param dataId 健康数据采集主键
     * @return 健康数据采集
     */
    public HealthData selectHealthDataById(Long dataId);

    /**
     * 查询健康数据采集列表
     * 
     * @param healthData 健康数据采集
     * @return 健康数据采集集合
     */
    public List<HealthData> selectHealthDataList(HealthData healthData);

    /**
     * 新增健康数据采集
     * 
     * @param healthData 健康数据采集
     * @return 结果
     */
    public int insertHealthData(HealthData healthData);

    /**
     * 修改健康数据采集
     * 
     * @param healthData 健康数据采集
     * @return 结果
     */
    public int updateHealthData(HealthData healthData);

    /**
     * 批量删除健康数据采集
     * 
     * @param dataIds 需要删除的健康数据采集主键集合
     * @return 结果
     */
    public int deleteHealthDataByIds(Long[] dataIds);

    /**
     * 删除健康数据采集信息
     * 
     * @param dataId 健康数据采集主键
     * @return 结果
     */
    public int deleteHealthDataById(Long dataId);
}
