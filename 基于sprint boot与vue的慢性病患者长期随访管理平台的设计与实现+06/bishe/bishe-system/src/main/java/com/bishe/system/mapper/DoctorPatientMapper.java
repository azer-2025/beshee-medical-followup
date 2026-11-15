package com.bishe.system.mapper;

import java.util.List;
import com.bishe.system.domain.DoctorPatient;
import com.bishe.common.core.domain.entity.SysUser;

/**
 * 医生患者关联Mapper接口
 * 
 * @author bishe
 */
public interface DoctorPatientMapper 
{
    /**
     * 查询医生名下的患者列表
     * 
     * @param user 用户信息
     * @return 患者集合
     */
    public List<SysUser> selectPatientListByDoctorId(SysUser user);

    /**
     * 新增医生患者关联
     * 
     * @param doctorPatient 医生患者关联
     * @return 结果
     */
    public int insertDoctorPatient(DoctorPatient doctorPatient);

    /**
     * 删除医生患者关联
     * 
     * @param doctorPatient 医生患者关联
     * @return 结果
     */
    public int deleteDoctorPatient(DoctorPatient doctorPatient);

    /**
     * 查询未绑定该医生的患者列表
     * 
     * @param doctorId 医生ID
     * @return 患者集合
     */
    public List<SysUser> selectUnboundPatientList(Long doctorId);

    /**
     * 查询患者的关联医生
     *
     * @param patientId 患者ID
     * @return 医生信息
     */
    public List<SysUser> selectDoctorByPatientId(Long patientId);
}
