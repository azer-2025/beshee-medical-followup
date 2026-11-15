package com.bishe.web.controller.followup;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bishe.common.annotation.Log;
import com.bishe.common.core.controller.BaseController;
import com.bishe.common.core.domain.AjaxResult;
import com.bishe.common.core.page.TableDataInfo;
import com.bishe.common.enums.BusinessType;
import com.bishe.common.utils.SecurityUtils;
import com.bishe.common.core.domain.entity.SysRole;
import com.bishe.system.domain.SysNotice;
import com.bishe.system.service.ISysNoticeService;

/**
 * 随访通知管理
 *
 * 管理员可查看全部通知并执行新增、修改、删除；
 * 医生和患者仅能查看与本人关联的随访通知。
 *
 * @author codex
 */
@RestController
@RequestMapping("/followup/notice")
public class FollowupNoticeController extends BaseController
{
    private static final String FOLLOWUP_NOTICE_TYPE = "1";

    @Autowired
    private ISysNoticeService noticeService;

    @PreAuthorize("@ss.hasAnyRoles('admin,doctor,patient')")
    @GetMapping("/list")
    public TableDataInfo list(SysNotice notice)
    {
        startPage();
        notice.setNoticeType(FOLLOWUP_NOTICE_TYPE);
        String remarkLike = buildRemarkLike();
        if (StringUtils.isNotBlank(remarkLike))
        {
            notice.getParams().put("remarkLike", remarkLike);
        }
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,doctor,patient')")
    @GetMapping("/{noticeId}")
    public AjaxResult getInfo(@PathVariable Long noticeId)
    {
        SysNotice notice = noticeService.selectNoticeById(noticeId);
        if (notice == null || !FOLLOWUP_NOTICE_TYPE.equals(notice.getNoticeType()))
        {
            return AjaxResult.error("随访通知不存在");
        }
        if (!canAccessNotice(notice))
        {
            return AjaxResult.error("无权查看该随访通知");
        }
        return success(notice);
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "随访通知", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysNotice notice)
    {
        notice.setNoticeType(FOLLOWUP_NOTICE_TYPE);
        notice.setCreateBy(getUsername());
        if (StringUtils.isBlank(notice.getRemark()))
        {
            notice.setRemark(buildManualRemark());
        }
        return toAjax(noticeService.insertNotice(notice));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "随访通知", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysNotice notice)
    {
        SysNotice currentNotice = noticeService.selectNoticeById(notice.getNoticeId());
        if (currentNotice == null || !FOLLOWUP_NOTICE_TYPE.equals(currentNotice.getNoticeType()))
        {
            return AjaxResult.error("随访通知不存在");
        }
        notice.setNoticeType(FOLLOWUP_NOTICE_TYPE);
        notice.setUpdateBy(getUsername());
        if (StringUtils.isBlank(notice.getRemark()))
        {
            notice.setRemark(StringUtils.defaultIfBlank(currentNotice.getRemark(), buildManualRemark()));
        }
        return toAjax(noticeService.updateNotice(notice));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "随访通知", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds)
    {
        for (Long noticeId : noticeIds)
        {
            SysNotice notice = noticeService.selectNoticeById(noticeId);
            if (notice == null || !FOLLOWUP_NOTICE_TYPE.equals(notice.getNoticeType()))
            {
                return AjaxResult.error("包含无效的随访通知数据");
            }
        }
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }

    private String buildRemarkLike()
    {
        if (isAdmin())
        {
            return null;
        }
        Long userId = SecurityUtils.getUserId();
        if (userId == null)
        {
            return null;
        }
        if (hasRole("doctor"))
        {
            return "doctorId:" + userId;
        }
        return "patientId:" + userId;
    }

    private boolean canAccessNotice(SysNotice notice)
    {
        if (isAdmin())
        {
            return true;
        }
        String remarkLike = buildRemarkLike();
        return StringUtils.isNotBlank(remarkLike) && StringUtils.contains(StringUtils.defaultString(notice.getRemark()), remarkLike);
    }

    private boolean isAdmin()
    {
        return SecurityUtils.getLoginUser() != null
            && SecurityUtils.getLoginUser().getUser() != null
            && SecurityUtils.getLoginUser().getUser().isAdmin();
    }

    private boolean hasRole(String roleKey)
    {
        if (SecurityUtils.getLoginUser() == null || SecurityUtils.getLoginUser().getUser() == null
            || SecurityUtils.getLoginUser().getUser().getRoles() == null)
        {
            return false;
        }
        for (SysRole role : SecurityUtils.getLoginUser().getUser().getRoles())
        {
            if (role != null && StringUtils.equals(roleKey, role.getRoleKey()))
            {
                return true;
            }
        }
        return false;
    }

    private String buildManualRemark()
    {
        return "followup-notice|manual|adminId:" + SecurityUtils.getUserId();
    }
}
