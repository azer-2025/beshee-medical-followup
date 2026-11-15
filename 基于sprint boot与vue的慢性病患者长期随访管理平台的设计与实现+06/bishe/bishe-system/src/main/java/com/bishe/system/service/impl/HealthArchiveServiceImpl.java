package com.bishe.system.service.impl;

import java.util.List;
import com.bishe.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bishe.system.mapper.HealthArchiveMapper;
import com.bishe.system.domain.HealthArchive;
import com.bishe.system.service.IHealthArchiveService;

/**
 * 电子健康档案Service业务层处理
 * 
 * @author bishe
 * @date 2025-05-22
 */
@Service
public class HealthArchiveServiceImpl implements IHealthArchiveService 
{
    @Autowired
    private HealthArchiveMapper healthArchiveMapper;

    /**
     * 查询电子健康档案
     * 
     * @param archiveId 电子健康档案主键
     * @return 电子健康档案
     */
    @Override
    public HealthArchive selectHealthArchiveById(Long archiveId)
    {
        return healthArchiveMapper.selectHealthArchiveById(archiveId);
    }

    /**
     * 查询电子健康档案列表
     * 
     * @param healthArchive 电子健康档案
     * @return 电子健康档案
     */
    @Override
    public List<HealthArchive> selectHealthArchiveList(HealthArchive healthArchive)
    {
        return healthArchiveMapper.selectHealthArchiveList(healthArchive);
    }

    /**
     * 新增电子健康档案
     * 
     * @param healthArchive 电子健康档案
     * @return 结果
     */
    @Override
    public int insertHealthArchive(HealthArchive healthArchive)
    {
        healthArchive.setCreateTime(DateUtils.getNowDate());
        return healthArchiveMapper.insertHealthArchive(healthArchive);
    }

    /**
     * 修改电子健康档案
     * 
     * @param healthArchive 电子健康档案
     * @return 结果
     */
    @Override
    public int updateHealthArchive(HealthArchive healthArchive)
    {
        healthArchive.setUpdateTime(DateUtils.getNowDate());
        return healthArchiveMapper.updateHealthArchive(healthArchive);
    }

    /**
     * 批量删除电子健康档案
     * 
     * @param archiveIds 需要删除的电子健康档案主键
     * @return 结果
     */
    @Override
    public int deleteHealthArchiveByIds(Long[] archiveIds)
    {
        return healthArchiveMapper.deleteHealthArchiveByIds(archiveIds);
    }

    /**
     * 删除电子健康档案信息
     * 
     * @param archiveId 电子健康档案主键
     * @return 结果
     */
    @Override
    public int deleteHealthArchiveById(Long archiveId)
    {
        return healthArchiveMapper.deleteHealthArchiveById(archiveId);
    }
}
