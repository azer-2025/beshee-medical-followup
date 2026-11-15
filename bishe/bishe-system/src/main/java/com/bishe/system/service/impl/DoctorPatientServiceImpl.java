package com.bishe.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bishe.system.mapper.DoctorPatientMapper;
import com.bishe.system.domain.DoctorPatient;
import com.bishe.system.service.IDoctorPatientService;
import com.bishe.common.core.domain.entity.SysUser;

/**
 * 医生患者关联Service业务层处理
 * 
 * @author bishe
 */
@Service
public class DoctorPatientServiceImpl implements IDoctorPatientService 
{
    @Autowired
    private DoctorPatientMapper doctorPatientMapper;

    @Override
    public List<SysUser> selectPatientListByDoctorId(SysUser user)
    {
        return doctorPatientMapper.selectPatientListByDoctorId(user);
    }

    @Override
    public List<SysUser> selectUnboundPatientList(Long doctorId)
    {
        return doctorPatientMapper.selectUnboundPatientList(doctorId);
    }

    @Override
    public int bindPatient(DoctorPatient doctorPatient)
    {
        return doctorPatientMapper.insertDoctorPatient(doctorPatient);
    }

    @Override
    public int unbindPatient(DoctorPatient doctorPatient)
    {
        return doctorPatientMapper.deleteDoctorPatient(doctorPatient);
    }

    @Override
    public List<SysUser> selectDoctorByPatientId(Long patientId)
    {
        return doctorPatientMapper.selectDoctorByPatientId(patientId);
    }
}
