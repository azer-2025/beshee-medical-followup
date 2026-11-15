package com.bishe.web.controller.library;

import java.util.HashMap;
import java.util.Map;

import com.bishe.common.core.controller.BaseController;
import com.bishe.common.core.domain.AjaxResult;
import com.bishe.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bishe.system.domain.LibraryReader;
import com.bishe.system.service.ILibraryBorrowRecordService;
import com.bishe.system.service.ILibraryReaderService;

@RestController
@RequestMapping("/library/portal")
public class LibraryPortalController extends BaseController
{
    @Autowired
    private ILibraryBorrowRecordService libraryBorrowRecordService;

    @Autowired
    private ILibraryReaderService libraryReaderService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> data = new HashMap<>();
        LibraryReader reader = libraryReaderService.ensureLibraryReaderByUserId(userId);
        data.put("reader", reader);
        data.put("summary", libraryBorrowRecordService.selectMyBorrowSummary(userId));
        return success(data);
    }
}
