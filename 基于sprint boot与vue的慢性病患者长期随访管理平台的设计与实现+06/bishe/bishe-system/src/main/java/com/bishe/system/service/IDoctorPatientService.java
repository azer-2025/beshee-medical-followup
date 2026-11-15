package com.bishe.system.service;

import java.util.List;
import com.bishe.system.domain.DoctorPatient;
import com.bishe.common.core.domain.entity.SysUser;

/**
 * 医生患者关联Service接口
 * 
 * @author bishe
 */
public interface IDoctorPatientService 
{
    /**
     * 查询医生名下的患者列表
     * 
     * @param user 用户信息
     * @return 患者集合
     */
    public List<SysUser> selectPatientListByDoctorId(SysUser user);

    /**
     * 查询未绑定该医生的患者列表
     * 
     * @param doctorId 医生ID
     * @return 患者集合
     */
    public List<SysUser> selectUnboundPatientList(Long doctorId);

    /**
     * 绑定患者
     * 
     * @param doctorPatient 关联信息
     * @return 结果
     */
    public int bindPatient(DoctorPatient doctorPatient);

    /**
     * 解绑患者
     * 
     * @param doctorPatient 关联信息
     * @return 结果
     */
    public int unbindPatient(DoctorPatient doctorPatient);

    /**
     * 查询患者的关联医生
     *
     * @param patientId 患者ID
     * @return 医生信息
     */
    public List<SysUser> selectDoctorByPatientId(Long patientId);
}
