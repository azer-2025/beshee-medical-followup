package com.bishe.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.bishe.common.constant.UserConstants;
import com.bishe.common.core.domain.entity.SysRole;
import com.bishe.common.core.domain.entity.SysUser;
import com.bishe.common.exception.ServiceException;
import com.bishe.common.utils.SecurityUtils;
import com.bishe.common.utils.StringUtils;
import com.bishe.common.utils.uuid.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bishe.system.domain.LibraryReader;
import com.bishe.system.mapper.LibraryBorrowRecordMapper;
import com.bishe.system.mapper.LibraryReaderMapper;
import com.bishe.system.service.ILibraryReaderService;
import com.bishe.system.service.ISysUserService;

@Service
public class LibraryReaderServiceImpl implements ILibraryReaderService
{
    private static final Long READER_ROLE_ID = 6L;

    @Autowired
    private LibraryReaderMapper libraryReaderMapper;

    @Autowired
    private LibraryBorrowRecordMapper libraryBorrowRecordMapper;

    @Autowired
    private ISysUserService userService;

    @Override
    public List<LibraryReader> selectLibraryReaderList(LibraryReader reader)
    {
        return libraryReaderMapper.selectLibraryReaderList(reader);
    }

    @Override
    public LibraryReader selectLibraryReaderById(Long readerId)
    {
        return libraryReaderMapper.selectLibraryReaderById(readerId);
    }

    @Override
    public LibraryReader selectLibraryReaderByUserId(Long userId)
    {
        return libraryReaderMapper.selectLibraryReaderByUserId(userId);
    }

    @Override
    @Transactional
    public LibraryReader ensureLibraryReaderByUserId(Long userId)
    {
        LibraryReader reader = libraryReaderMapper.selectLibraryReaderByUserId(userId);
        if (StringUtils.isNotNull(reader))
        {
            return reader;
        }

        SysUser user = userService.selectUserById(userId);
        if (StringUtils.isNull(user) || !isReaderUser(user))
        {
            return null;
        }

        reader = new LibraryReader();
        reader.setUserId(user.getUserId());
        reader.setReaderNo(buildReaderNo(null));
        reader.setCardNo(buildCardNo());
        reader.setBorrowLimit(5);
        reader.setStatus(StringUtils.isEmpty(user.getStatus()) ? UserConstants.NORMAL : user.getStatus());
        reader.setCreateBy(StringUtils.defaultIfEmpty(user.getUserName(), "system"));
        reader.setRemark("系统自动开通读者档案");
        libraryReaderMapper.insertLibraryReader(reader);
        return libraryReaderMapper.selectLibraryReaderByUserId(userId);
    }

    @Override
    @Transactional
    public int insertLibraryReader(LibraryReader reader)
    {
        checkReaderUnique(reader);

        SysUser user = new SysUser();
        user.setUserName(reader.getUserName());
        user.setNickName(reader.getNickName());
        user.setPhonenumber(reader.getPhonenumber());
        user.setEmail(reader.getEmail());
        user.setSex(reader.getSex());
        user.setAvatar(reader.getAvatar());
        user.setStatus(StringUtils.isEmpty(reader.getStatus()) ? UserConstants.NORMAL : reader.getStatus());
        user.setPassword(SecurityUtils.encryptPassword(reader.getPassword()));
        user.setRoleIds(new Long[] { READER_ROLE_ID });
        user.setCreateBy(reader.getCreateBy());
        user.setRemark(reader.getRemark());
        userService.insertUser(user);

        reader.setUserId(user.getUserId());
        reader.setReaderNo(buildReaderNo(reader.getReaderNo()));
        reader.setBorrowLimit(StringUtils.isNull(reader.getBorrowLimit()) ? 5 : reader.getBorrowLimit());
        reader.setStatus(user.getStatus());
        return libraryReaderMapper.insertLibraryReader(reader);
    }

    @Override
    @Transactional
    public int updateLibraryReader(LibraryReader reader)
    {
        LibraryReader oldReader = libraryReaderMapper.selectLibraryReaderById(reader.getReaderId());
        if (StringUtils.isNull(oldReader))
        {
            throw new ServiceException("读者信息不存在");
        }
        checkReaderUnique(reader);

        SysUser user = userService.selectUserById(oldReader.getUserId());
        user.setUserName(reader.getUserName());
        user.setNickName(reader.getNickName());
        user.setPhonenumber(reader.getPhonenumber());
        user.setEmail(reader.getEmail());
        user.setSex(reader.getSex());
        user.setAvatar(reader.getAvatar());
        user.setStatus(reader.getStatus());
        user.setRemark(reader.getRemark());
        user.setUpdateBy(reader.getUpdateBy());
        user.setRoleIds(new Long[] { READER_ROLE_ID });
        if (StringUtils.isNotEmpty(reader.getPassword()))
        {
            user.setPassword(SecurityUtils.encryptPassword(reader.getPassword()));
        }
        userService.updateUser(user);

        reader.setUserId(oldReader.getUserId());
        reader.setReaderNo(buildReaderNo(reader.getReaderNo()));
        reader.setBorrowLimit(StringUtils.isNull(reader.getBorrowLimit()) ? 5 : reader.getBorrowLimit());
        return libraryReaderMapper.updateLibraryReader(reader);
    }

    @Override
    @Transactional
    public int deleteLibraryReaderByIds(Long[] readerIds)
    {
        if (libraryBorrowRecordMapper.countActiveBorrowByReaderIds(readerIds) > 0)
        {
            throw new ServiceException("存在未归还借阅记录，不能删除读者");
        }
        List<Long> userIds = new ArrayList<>();
        for (Long readerId : readerIds)
        {
            LibraryReader reader = libraryReaderMapper.selectLibraryReaderById(readerId);
            if (StringUtils.isNotNull(reader))
            {
                userIds.add(reader.getUserId());
            }
        }
        libraryReaderMapper.deleteLibraryReaderByIds(readerIds);
        for (Long userId : userIds)
        {
            userService.deleteUserById(userId);
        }
        return readerIds.length;
    }

    private void checkReaderUnique(LibraryReader reader)
    {
        if (StringUtils.isEmpty(reader.getUserName()))
        {
            throw new ServiceException("登录账号不能为空");
        }
        if (StringUtils.isEmpty(reader.getNickName()))
        {
            throw new ServiceException("读者姓名不能为空");
        }
        if (StringUtils.isNull(reader.getReaderId()) && StringUtils.isEmpty(reader.getPassword()))
        {
            throw new ServiceException("新增读者时密码不能为空");
        }

        SysUser checkUser = new SysUser();
        checkUser.setUserId(getCurrentUserId(reader.getReaderId()));
        checkUser.setUserName(reader.getUserName());
        checkUser.setPhonenumber(reader.getPhonenumber());
        checkUser.setEmail(reader.getEmail());

        if (!userService.checkUserNameUnique(checkUser))
        {
            throw new ServiceException("登录账号已存在");
        }
        if (StringUtils.isNotEmpty(reader.getPhonenumber()) && !userService.checkPhoneUnique(checkUser))
        {
            throw new ServiceException("手机号已存在");
        }
        if (StringUtils.isNotEmpty(reader.getEmail()) && !userService.checkEmailUnique(checkUser))
        {
            throw new ServiceException("邮箱已存在");
        }
        if (StringUtils.isNotEmpty(reader.getReaderNo()))
        {
            LibraryReader exists = libraryReaderMapper.selectLibraryReaderByReaderNo(reader.getReaderNo());
            if (StringUtils.isNotNull(exists) && !exists.getReaderId().equals(reader.getReaderId()))
            {
                throw new ServiceException("读者证号已存在");
            }
        }
    }

    private Long getCurrentUserId(Long readerId)
    {
        if (StringUtils.isNull(readerId))
        {
            return null;
        }
        LibraryReader reader = libraryReaderMapper.selectLibraryReaderById(readerId);
        return StringUtils.isNotNull(reader) ? reader.getUserId() : null;
    }

    private String buildReaderNo(String readerNo)
    {
        if (StringUtils.isNotEmpty(readerNo))
        {
            return readerNo;
        }
        return "RD" + IdUtils.fastSimpleUUID().substring(0, 10).toUpperCase();
    }

    private String buildCardNo()
    {
        return "CARD" + IdUtils.fastSimpleUUID().substring(0, 8).toUpperCase();
    }

    private boolean isReaderUser(SysUser user)
    {
        if (Objects.equals("2", user.getDelFlag()) || !Objects.equals(UserConstants.NORMAL, user.getStatus()))
        {
            return false;
        }
        if (user.getRoles() == null || user.getRoles().isEmpty())
        {
            return false;
        }
        return user.getRoles().stream()
            .filter(Objects::nonNull)
            .map(SysRole::getRoleKey)
            .anyMatch("reader"::equals);
    }
}
