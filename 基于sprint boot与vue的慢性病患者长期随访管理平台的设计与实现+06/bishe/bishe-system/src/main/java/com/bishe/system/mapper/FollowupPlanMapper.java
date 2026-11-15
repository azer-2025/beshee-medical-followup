package com.bishe.system.mapper;

import java.util.List;
import com.bishe.system.domain.FollowupPlan;

/**
 * 随访计划Mapper接口
 * 
 * @author bishe
 * @date 2025-05-22
 */
public interface FollowupPlanMapper 
{
    /**
     * 查询随访计划
     * 
     * @param planId 随访计划主键
     * @return 随访计划
     */
    public FollowupPlan selectFollowupPlanById(Long planId);

    /**
     * 查询随访计划列表
     * 
     * @param followupPlan 随访计划
     * @return 随访计划集合
     */
    public List<FollowupPlan> selectFollowupPlanList(FollowupPlan followupPlan);

    /**
     * 新增随访计划
     * 
     * @param followupPlan 随访计划
     * @return 结果
     */
    public int insertFollowupPlan(FollowupPlan followupPlan);

    /**
     * 修改随访计划
     * 
     * @param followupPlan 随访计划
     * @return 结果
     */
    public int updateFollowupPlan(FollowupPlan followupPlan);

    /**
     * 删除随访计划
     * 
     * @param planId 随访计划主键
     * @return 结果
     */
    public int deleteFollowupPlanById(Long planId);

    /**
     * 批量删除随访计划
     * 
     * @param planIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFollowupPlanByIds(Long[] planIds);

    /**
     * 自动更新过期计划为已完成
     * @return 结果
     */
    public int updateOverduePlansToCompleted();
}
