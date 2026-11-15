package com.bishe.system.service.impl;

import java.util.List;
import com.bishe.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bishe.system.mapper.FollowupPlanMapper;
import com.bishe.system.domain.FollowupPlan;
import com.bishe.system.service.IFollowupPlanService;

/**
 * 随访计划Service业务层处理
 * 
 * @author bishe
 * @date 2025-05-22
 */
@Service
public class FollowupPlanServiceImpl implements IFollowupPlanService 
{
    @Autowired
    private FollowupPlanMapper followupPlanMapper;

    /**
     * 查询随访计划
     * 
     * @param planId 随访计划主键
     * @return 随访计划
     */
    @Override
    public FollowupPlan selectFollowupPlanById(Long planId)
    {
        return followupPlanMapper.selectFollowupPlanById(planId);
    }

    /**
     * 查询随访计划列表
     * 
     * @param followupPlan 随访计划
     * @return 随访计划
     */
    @Override
    public List<FollowupPlan> selectFollowupPlanList(FollowupPlan followupPlan)
    {
        updateOverduePlansToCompleted();
        return followupPlanMapper.selectFollowupPlanList(followupPlan);
    }

    /**
     * 自动更新过期计划为已完成
     */
    @Override
    public void updateOverduePlansToCompleted()
    {
        followupPlanMapper.updateOverduePlansToCompleted();
    }

    /**
     * 新增随访计划
     * 
     * @param followupPlan 随访计划
     * @return 结果
     */
    @Override
    public int insertFollowupPlan(FollowupPlan followupPlan)
    {
        followupPlan.setCreateTime(DateUtils.getNowDate());
        return followupPlanMapper.insertFollowupPlan(followupPlan);
    }

    /**
     * 修改随访计划
     * 
     * @param followupPlan 随访计划
     * @return 结果
     */
    @Override
    public int updateFollowupPlan(FollowupPlan followupPlan)
    {
        return followupPlanMapper.updateFollowupPlan(followupPlan);
    }

    /**
     * 批量删除随访计划
     * 
     * @param planIds 需要删除的随访计划主键
     * @return 结果
     */
    @Override
    public int deleteFollowupPlanByIds(Long[] planIds)
    {
        return followupPlanMapper.deleteFollowupPlanByIds(planIds);
    }

    /**
     * 删除随访计划信息
     * 
     * @param planId 随访计划主键
     * @return 结果
     */
    @Override
    public int deleteFollowupPlanById(Long planId)
    {
        return followupPlanMapper.deleteFollowupPlanById(planId);
    }
}
