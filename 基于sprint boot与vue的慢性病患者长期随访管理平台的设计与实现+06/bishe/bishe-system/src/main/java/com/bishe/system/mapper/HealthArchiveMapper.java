package com.bishe.system.mapper;

import java.util.List;
import com.bishe.system.domain.HealthArchive;

/**
 * 电子健康档案Mapper接口
 * 
 * @author bishe
 * @date 2025-05-22
 */
public interface HealthArchiveMapper 
{
    /**
     * 查询电子健康档案
     * 
     * @param archiveId 电子健康档案主键
     * @return 电子健康档案
     */
    public HealthArchive selectHealthArchiveById(Long archiveId);

    /**
     * 查询电子健康档案列表
     * 
     * @param healthArchive 电子健康档案
     * @return 电子健康档案集合
     */
    public List<HealthArchive> selectHealthArchiveList(HealthArchive healthArchive);

    /**
     * 新增电子健康档案
     * 
     * @param healthArchive 电子健康档案
     * @return 结果
     */
    public int insertHealthArchive(HealthArchive healthArchive);

    /**
     * 修改电子健康档案
     * 
     * @param healthArchive 电子健康档案
     * @return 结果
     */
    public int updateHealthArchive(HealthArchive healthArchive);

    /**
     * 删除电子健康档案
     * 
     * @param archiveId 电子健康档案主键
     * @return 结果
     */
    public int deleteHealthArchiveById(Long archiveId);

    /**
     * 批量删除电子健康档案
     * 
     * @param archiveIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHealthArchiveByIds(Long[] archiveIds);
}
