package com.bishe.web.controller.system;

import com.bishe.common.core.controller.BaseController;
import com.bishe.common.core.domain.AjaxResult;
import com.bishe.common.core.domain.entity.SysUser;
import com.bishe.common.core.page.TableDataInfo;
import com.bishe.common.utils.SecurityUtils;
import com.bishe.common.utils.StringUtils;
import com.bishe.system.domain.DoctorPatient;
import com.bishe.system.service.IDoctorPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.bishe.system.service.ISysRoleService;
import com.bishe.system.service.ISysUserService;

import java.util.List;

/**
 * 医生患者关联操作处理
 *
 * @author bishe
 */
@RestController
@RequestMapping("/system/doctorPatient")
public class DoctorPatientController extends BaseController
{
    @Autowired
    private IDoctorPatientService doctorPatientService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    /**
     * 获取医生名下的患者列表
     */
    @PreAuthorize("@ss.hasRole('doctor') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user)
    {
        startPage();
        Long doctorId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin() || SecurityUtils.hasRole("admin")) {
            doctorId = null;
        } else {
             user.getParams().put("doctorId", doctorId);
        }
        
        List<SysUser> list = doctorPatientService.selectPatientListByDoctorId(user);
        return getDataTable(list);
    }

    /**
     * 获取未绑定当前医生的患者列表
     */
    @PreAuthorize("@ss.hasRole('doctor') or @ss.hasRole('admin')")
    @GetMapping("/unboundList")
    public AjaxResult unboundList(Long doctorId)
    {
        if (doctorId == null && !SecurityUtils.isAdmin() && !SecurityUtils.hasRole("admin")) {
             doctorId = SecurityUtils.getUserId();
        }
        List<SysUser> list = doctorPatientService.selectUnboundPatientList(doctorId);
        return success(list);
    }

    /**
     * 绑定患者
     */
    @PreAuthorize("@ss.hasRole('doctor') or @ss.hasRole('admin')")
    @PostMapping("/bind")
    public AjaxResult bind(@RequestBody DoctorPatient doctorPatient)
    {
        if (doctorPatient.getDoctorId() == null) {
            doctorPatient.setDoctorId(SecurityUtils.getUserId());
        }
        return toAjax(doctorPatientService.bindPatient(doctorPatient));
    }

    /**
     * 解绑患者
     */
    @PreAuthorize("@ss.hasRole('doctor') or @ss.hasRole('admin')")
    @DeleteMapping("/unbind/{patientId}")
    public AjaxResult unbind(@PathVariable Long patientId, @RequestParam(required = false) Long doctorId)
    {
        DoctorPatient doctorPatient = new DoctorPatient();
        if (doctorId != null && (SecurityUtils.isAdmin() || SecurityUtils.hasRole("admin"))) {
             doctorPatient.setDoctorId(doctorId);
        } else {
             doctorPatient.setDoctorId(SecurityUtils.getUserId());
        }
        doctorPatient.setPatientId(patientId);
        return toAjax(doctorPatientService.unbindPatient(doctorPatient));
    }

    /**
     * 新增患者 (医生创建)
     */
    @PreAuthorize("@ss.hasRole('doctor') or @ss.hasRole('admin')")
    @PostMapping("/add")
    public AjaxResult add(@Validated @RequestBody SysUser user)
    {
        if (!userService.checkUserNameUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }

        user.setCreateBy(getUsername());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        // 强制设置为患者角色 (ID 4)
        user.setRoleIds(new Long[]{4L});

        // 1. 创建用户
        int rows = userService.insertUser(user);

        // 2. 自动绑定到当前医生
        if (rows > 0) {
            DoctorPatient dp = new DoctorPatient();
            dp.setDoctorId(SecurityUtils.getUserId());
            dp.setPatientId(user.getUserId());
            doctorPatientService.bindPatient(dp);
        }

        return toAjax(rows);
    }

    /**
     * 修改患者信息
     */
    @PreAuthorize("@ss.hasRole('doctor') or @ss.hasRole('admin')")
    @PutMapping("/edit")
    public AjaxResult edit(@Validated @RequestBody SysUser user)
    {
        if (!userService.checkUserNameUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUser(user));
    }

    /**
     * 获取医生列表 (供患者选择)
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listDoctors")
    public TableDataInfo listDoctors(SysUser user)
    {
        startPage();
        user.getParams().put("roleKey", "doctor");
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    /**
     * 获取我的主治医生
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myDoctor")
    public AjaxResult getMyDoctor()
    {
        Long patientId = SecurityUtils.getUserId();
        List<SysUser> doctors = doctorPatientService.selectDoctorByPatientId(patientId);
        if (StringUtils.isNotEmpty(doctors)) {
            return success(doctors.get(0));
        }
        return success();
    }

    /**
     * 患者选择主治医生
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/selectDoctor")
    public AjaxResult selectDoctor(@RequestBody DoctorPatient doctorPatient)
    {
        Long patientId = SecurityUtils.getUserId();
        doctorPatient.setPatientId(patientId);

        if (doctorPatient.getDoctorId() == null) {
            return error("请选择医生");
        }

        // 1. Unbind existing doctor (if any)
        // We need to know the OLD doctor ID to unbind specifically, or we need a method to unbind ALL by patientId.
        // The current unbindPatient takes (doctorId, patientId).
        // Let's first find the current doctor.
        List<SysUser> currentDoctors = doctorPatientService.selectDoctorByPatientId(patientId);
        if (StringUtils.isNotEmpty(currentDoctors)) {
            for (SysUser doctor : currentDoctors) {
                DoctorPatient oldDp = new DoctorPatient();
                oldDp.setDoctorId(doctor.getUserId());
                oldDp.setPatientId(patientId);
                doctorPatientService.unbindPatient(oldDp);
            }
        }

        // 2. Bind new doctor
        return toAjax(doctorPatientService.bindPatient(doctorPatient));
    }
}
